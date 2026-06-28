import mongoose from "mongoose";

const bookingSchema = new mongoose.Schema({
  vehicleId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Vehicle",
    required: true,
  },
  pickupDate: { type: Date, required: true },
  dropOffDate: { type: Date, required: true },
  userId: { type: mongoose.Schema.Types.ObjectId, ref: "User" },
  pickUpLocation: { type: String, required: true },
  dropOffLocation: { type: String, required: true },
  pickUpDistrict: { type: String, required: false },
  totalPrice: { type: Number, required: true },
  createdAt: { type: Date, default: Date.now },
  status: {
    type: String,
    enum: ["notBooked", "booked", "onTrip", "notPicked", "canceled", "overDue", "tripCompleted"],
    default: "notBooked"
  }
});

const Booking = mongoose.model("Booking", bookingSchema);

export default Booking;
