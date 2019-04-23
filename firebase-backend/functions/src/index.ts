import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp()

// Start writing Firebase Functions
// https://firebase.google.com/docs/functions/typescript

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

export const onUserPinUpdate = functions.database
.ref('/users/{uid}/pin')
.onUpdate(async (change, context) => {
    const uid = context.params.uid
    const pin_after = change.after

    if(pin_after.val() !== -1) {
        return null
    }

    try{
        
        const wallet_balance = await admin.database().ref('users/' + uid).child('wallet').once('value')
        const current_item_cost = await admin.database().ref('users/' + uid).child('cicost').once('value')

        if(current_item_cost.val() > wallet_balance.val()) {
            console.log('the cost of the item is higer than the wallet balance')
            return admin.database().ref('users/' + uid).ref.update({pin:-2})
        } else {
            console.log('the cost of the item is higer than the wallet balance')
            await admin.database().ref('users/' + uid).ref.update({wallet:wallet_balance.val() - current_item_cost.val()})
        }

        const mid = await admin.database().ref('users/' + uid).child('mid').once('value')

        const pin = await getPin(mid.val())
        return admin.database().ref('users/' + uid).ref.update({pin:pin}) 

    } catch (error) {
        console.log("Error : " + error)
        return null
    }

})

async function getPin(mid : string) {
    return '1234'
}