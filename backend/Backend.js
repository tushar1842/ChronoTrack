const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
require("dotenv").config();

const History = require("./models/History");

const app = express();

app.use(cors());
app.use(express.json());

// MongoDB Connection
mongoose.connect(process.env.MONGO_URI)
    .then(() => console.log("MongoDB Connected Successfully"))
    .catch(err => console.log("MongoDB Error:", err));

// Test API
app.get("/", (req, res) => {
    res.send("ChronoTrack Backend is Running!");
});

// Save browsing history
app.post("/api/history", async (req, res) => {
    try {
        const { url, title, browserMode } = req.body;

        if (!url || !["normal", "incognito"].includes(browserMode)) {
            return res.status(400).json({
                message: "Invalid history data"
            });
        }

        const domain = new URL(url).hostname;

        const record = await History.create({
            url,
            domain,
            title,
            browserMode
        });

        res.status(201).json({
            message: "History saved",
            data: record
        });

    } catch (error) {
        res.status(500).json({
            message: error.message
        });
    }
});

// Get browsing history
app.get("/api/history", async (req, res) => {
    try {
        const records = await History.find()
            .sort({ visitTime: -1 });

        res.json(records);

    } catch (error) {
        res.status(500).json({
            message: error.message
        });
    }
});

app.listen(5000, "127.0.0.1", () => {
    console.log("ChronoTrack server running on port 5000");
});