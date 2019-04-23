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