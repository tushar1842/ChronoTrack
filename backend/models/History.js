const mongoose = require("mongoose");

const historySchema = new mongoose.Schema({
    url: {
        type: String,
        required: true
    },

    domain: {
        type: String
    },

    title: {
        type: String
    },

    browserMode: {
        type: String,
        enum: ["normal", "incognito"],
        required: true
    },

    visitTime: {
        type: Date,
        default: Date.now
    }
});

module.exports = mongoose.model("History", historySchema);