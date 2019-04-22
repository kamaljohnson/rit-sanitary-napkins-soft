import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { DataSnapshot } from 'firebase-functions/lib/providers/database';

admin.initializeApp()

// Start writing Firebase Functions
// https://firebase.google.com/docs/functions/typescript
DataSnapshot
export const onUserDateTrigger = functions.database
.ref('/users/{uid}')
.onUpdate(async (change, context) => {

    const before = change.before
    const after = change.after

    if(before.val() === after.val()) {
        return null
    }
    
    const mid_before = before.child('mid').val()
    const pin_before = before.child('pin').val()
    const ctcost_before = before.child('ctcost').val()
    const wallet_before = before.child('wallet').val()

    const mid_after = after.child('mid').val()
    const pin_after = after.child('pin').val()
    const ctcost_after = after.child('ctcost').val()
    const wallet_after = after.child('wallet').val()

    if(mid_before !== mid_after){

        const data = admin.database().ref('/machines/{mid}').once('value')
        console.log(data)

    }else if(pin_before !== pin_after) {

        return after.ref.update({pin:"pin was changed"})

    }else if(ctcost_after !== ctcost_before) {

        return after.ref.update({ctcost:"ctcost was changed"})
        
    }else if(wallet_after !== wallet_before) {

        return after.ref.update({wallet:"wallet was changed"})
        
    }
    return null
})


//------------------Helper functions-------------------
