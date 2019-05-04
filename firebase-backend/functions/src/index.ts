import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { sha256 } from 'js-sha256';

admin.initializeApp()

export const onUserMidUpdate = functions.database
.ref('/users/{uid}/mid')
.onUpdate((change, context) => {
    const uid = context.params.uid
    const mid_after = change.after
    const mid_before = change.before
    
    if(mid_after.val() === mid_before.val()) {
        return null
    }

    const machineRef = admin.database().ref('machines/' + mid_after.val());
    return machineRef.once('value')
    .then(async snapshot => {
        if(snapshot.exists()) {
            try{
                const item_cost = await machineRef.once('value') 
                return admin.database().ref('users/' + uid).ref.update({cicost:item_cost.child('item_cost').val()})
            } catch(error) {
                console.log("Error : " + error)
                return null
            }
        }
        else {
            return admin.database().ref('users/' + uid).ref.update({cicost:-1})
        }
    })
    .catch(error => {
        console.log("Error : " + error)
        return null
    })
})

// pin updations : -2 not enough balance, -3 pin generation error
export const onUserPinUpdate = functions.database
.ref('/users/{uid}/pin')
.onUpdate(async (change, context) => {
    const uid = context.params.uid
    const pin_after = change.after

    if(pin_after.val() !== '-1') {
        return null
    }

    try{
        const wallet_balance = await admin.database().ref('users/' + uid).child('wallet').once('value')
        const current_item_cost = await admin.database().ref('users/' + uid).child('cicost').once('value')

        if(current_item_cost.val() > wallet_balance.val()) {
            return admin.database().ref('users/' + uid).ref.update({pin:'-2'})
        }

        const mid = await admin.database().ref('users/' + uid).child('mid').once('value')

        if(mid.val() === "") {
            return admin.database().ref('users/' + uid).ref.update({pin:'-3'})
        }
        const machineRef = admin.database().ref('machines/' + mid.val());
        
        const mid_snap =  await machineRef.once('value')    
        
        if(!mid_snap.exists()) {
            return admin.database().ref('users/' + uid).ref.update({pin:'-3'})
        }
        
        const pin = await getPin(mid.val())

        await admin.database().ref('users/' + uid).ref.update({wallet:wallet_balance.val() - current_item_cost.val()})
        await admin.database().ref('users/' + uid).ref.update({mid:""})
        await admin.database().ref('users/' + uid).ref.update({cicost:-1})
        
        return admin.database().ref('users/' + uid).ref.update({pin:pin})
    } catch (error) {
        console.log("Error : " + error)
        return null
    }

})

export const onTransactionCreate = functions.database
.ref('/transactions/{pushId}')
.onCreate(async (snapshot, context) => {
    const transaction_id = context.params.pushId
    const to = snapshot.child('to')
    const from = snapshot.child('from')

    const company_upi = await admin.database().ref('banks/company').child('upi').once('value')

    if(to.val() === company_upi.val()) {
        return admin.database().ref('transactions/' + transaction_id).ref.update({status:"valid"})
    }

    //checking if the to uid is a valid user id
    const to_user_ref = admin.database().ref('users/' + to.val());
    return to_user_ref.once('value')
    .then(async snap => {
        if(snap.exists() && (to.val() !== from.val())) {
            try{
                return admin.database().ref('transactions/' + transaction_id).ref.update({status:"valid"})
            } catch(error) {
                console.log("Error : " + error)
                return null
            }
        }
        else {
            return admin.database().ref('transactions/' + transaction_id).ref.update({status:"invalid"})
        }
    })
    .catch(error => {
        console.log("Error : " + error)
        return null
    })

})

export const onTransactionUpdate = functions.database
.ref('/transactions/{pushId}')
.onUpdate(async (change, context) => {
    const amount = change.after.child('amount').val()
    const to = change.after.child('to').val()
    const from = change.after.child('from').val()

    if(change.after.child('amount').val() === change.before.child('amount').val() || change.after.child('amount').val() === 0) {
        return null
    }

    const to_user_ref = admin.database().ref('users/' + to);
    const from_user_ref  = admin.database().ref('users/' + from);

    const company_ref = await admin.database().ref('banks/company').once('value')

    if(to === company_ref.child('upi').val()) {
        //TODO: check using google pay api for payment validation
        const status = change.after.child('status').val()
        switch(status) {
            case "success":
                const from_user_wallet_balance = await admin.database().ref('users/' + from).child('wallet').once('value')  
                await from_user_ref.update({wallet:from_user_wallet_balance.val() + amount})
                await admin.database().ref('transactions/' + context.params.pushId).ref.update({status:"Successful"})
                return admin.database().ref('banks/company').ref.update({balance:company_ref.child('balance').val() + amount})
            case "cancelled":
                return admin.database().ref('transactions/' + context.params.pushId).ref.update({status:"Cancelled"})
            case "failed":
                return admin.database().ref('transactions/' + context.params.pushId).ref.update({status:"Failed"})
        }
        
    }

    try{
        const promises = []
        const to_user_wallet_balance = await admin.database().ref('users/' + to).child('wallet').once('value')
        const from_user_wallet_balance = await admin.database().ref('users/' + from).child('wallet').once('value')
        
        const status = admin.database().ref('transactions/' + context.params.pushId)

        if((from_user_wallet_balance.val() - amount) < 0) {
            return status.update({status:"insufficient funds"})    
        }

        promises.push(to_user_ref.update({wallet:(to_user_wallet_balance.val() + amount)}))
    
        promises.push(from_user_ref.update({wallet:(from_user_wallet_balance.val() - amount)}))

        promises.push(status.update({status:"Successful"}))
        return Promise.all(promises)
    } catch(error) {
        const status = await admin.database().ref('transactions/' + context.params.pushId)
        await status.update({status:"Error"})
        return("Error : " + error)
    }

})


// Un comment this when upgrading to a heigher firebase plane

// export const onFeedbackCreate = functions.database
// .ref('/feedbacks/{pushId}')
// .onCreate(async (snapshot, context) => {
    
//     const message = snapshot.child('message').val()
//     const time_stamp = snapshot.child('time stamp').val()
//     const uid = snapshot.child('uid').val()
    
//     const email_msg = {
//         to: "xborggames@gmail.com",
//         from: "kamaljohnson12345@gmail.com",
//         subject: "Feedback",

//         templateId:'d-3098ae8783ef4b6eb7fa4146a030a777',
//         substitutionWrappers:['{{', '}}'],
//         substitution: {
//             time_stamp: time_stamp,
//             uid: uid,
//             message: message
//         }
//     }
//     return sgMail.send(email_msg)
// })


async function getPin(mid : string) {
    
    try{
        const pin_gen_code =  await admin.database().ref('machines/' + mid).child('pgcode').once('value')
        const prev_sales =  await admin.database().ref('machines/' + mid).child('sales').once('value')
        const new_pin_gen_code = pin_gen_code.val() + 1
        await admin.database().ref('machines/' + mid).ref.update({pgcode: new_pin_gen_code})
        await admin.database().ref('machines/' + mid).ref.update({sales: prev_sales.val() + 1})
        
        //the hash of the generation code and mid of the machine
        let hash = sha256(String(pin_gen_code.val()) + mid)
        hash = hash.toUpperCase()
        
        //getting the 3digits of the hash
        let pin = ""
        for (let i = 0; i<5; i++) {
            pin +=  String(hash.charCodeAt(i) % 10) + " "
        }
        pin += String(hash.charCodeAt(5)%10)

        return pin

    } catch (error) {
        console.log("Error : " + error)
        return '-3'
    }

}