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
    .then(snapshot => {
        if(snapshot.exists()) {
            return admin.database().ref('users/' + uid).ref.update({cost:10})
        }
        else {
            return admin.database().ref('users/' + uid).ref.update({cost:-1})
        }
    })
    .catch(error => {
        console.log("Error : " + error)
        return null
    })
})