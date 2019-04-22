const functions = require('firebase-functions');
const admin = require('firebase-admin')
admin.initializeApp(functions.config().firebase);

exports.onUserDataUpdateTrigger.onRequest((req, res) => {
    const text = req.query.text;
    admin.database().ref('/test').push({text: text}).then(shapshot => {
        res.redirect(303, snapshot.ref);        
    })
})