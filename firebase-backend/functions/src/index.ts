import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { DataSnapshot } from 'firebase-functions/lib/providers/database';
admin.initializeApp()

// Start writing Firebase Functions
// https://firebase.google.com/docs/functions/typescript

export const onMachineScanTrigger = functions.database
.ref('/users/{uid}/mid')
.onUpdate((snapshot, context) => {
    const uid = context.params.uid
    const mid = snapshot.after.val().toString()

    var machine_status:number = getMachineItemCost(mid)
    console.log(`the cost of the item from the machine ${mid} is ${machine_status} Rs`)
})

function getMachineItemCost(mid:number) {
    const db = admin.database().ref('/machines');
    
    const query = db.orderByChild('mid').equalTo(mid.toString()).limitToFirst(1);

    if(query == null) {
        return(0)
    }    
    else {
        var cost:number = 10
        //TODO: get the cost of the item from the database
        return(cost)
    }

}