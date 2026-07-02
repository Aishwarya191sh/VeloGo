import mongoose from "mongoose";
import Booking from "../../models/BookingModel.js";
import { errorHandler } from "../../utils/error.js";
import Razorpay from "razorpay";
import { availableAtDate } from "../../services/checkAvailableVehicle.js";
import Vehicle from "../../models/vehicleModel.js";
import nodemailer from "nodemailer";

export const BookCar = async (req, res, next) => {
  try {
    if (!req.body) {
      return next(errorHandler(401, "bad request on body"));
    }

    const {
      user_id,
      vehicle_id,
      totalPrice,
      pickupDate,
      dropoffDate,
      pickup_location,
      dropoff_location,
      pickup_district,
      razorpayPaymentId,
      razorpayOrderId,
    } = req.body;

    const book = new Booking({
      pickupDate,
      dropOffDate: dropoffDate,
      userId: user_id,
      pickUpLocation: pickup_location,
      vehicleId: vehicle_id,
      dropOffLocation: dropoff_location,
      pickUpDistrict: pickup_district,
      totalPrice,
      razorpayPaymentId,
      razorpayOrderId,
      status: "booked",
    });

    const booked = await book.save();
    res.status(200).json({
      message: "car booked successfully",
      booked,
    });
  } catch (error) {
    console.error("Book car error:", error);
    next(errorHandler(500, "error while booking car"));
  }
};

// creating razorpay order instance
export const razorpayOrder = async (req, res, next) => {
  try {
    const { totalPrice, dropoff_location, pickup_district, pickup_location } = req.body;

    if (
      !totalPrice ||
      !dropoff_location ||
      !pickup_district ||
      !pickup_location
    ) {
      return next(errorHandler(400, "Missing Required Feilds Process Cancelled"));
    }

    const instance = new Razorpay({
      key_id: process.env.RAZORPAY_KEY_ID,
      key_secret: process.env.RAZORPAY_SECRET,
    });

    const options = {
      amount: Math.round(totalPrice * 100), // amount in smallest currency unit
      currency: "INR",
    };

    const order = await instance.orders.create(options);

    if (!order) return res.status(500).send("Some error occured");
    res.status(200).json(order);
  } catch (error) {
    console.error("Razorpay order creation error:", error);
    next(errorHandler(500, "error occured in razorpayorder"));
  }
};

// getting vehicles without booking for selected Date and location
export const getVehiclesWithoutBooking = async (req, res, next) => {
  try {
    const { pickUpDistrict, pickUpLocation, pickupDate, dropOffDate, model } = req.body;

    if (!pickUpDistrict || !pickUpLocation)
      return next(errorHandler(409, "pickup District and location needed"));

    if (!pickupDate || !dropOffDate)
      return next(errorHandler(409, "pickup , dropffdate  is required"));

    // Check if pickupDate is before dropOffDate
    if (new Date(pickupDate) >= new Date(dropOffDate))
      return next(errorHandler(409, "Invalid date range"));

    const vehiclesAvailableAtDate = await availableAtDate(
      pickupDate,
      dropOffDate
    );

    if (!vehiclesAvailableAtDate) {
      return res.status(404).json({
        success: false,
        message: "No vehicles available for the specified time period.",
      });
    }

    const availableVehicles = vehiclesAvailableAtDate.filter(
      (cur) =>
        cur.district === pickUpDistrict &&
        cur.location === pickUpLocation &&
        cur.isDeleted === "false" &&
        cur.isAdminApproved === true &&
        cur.isRejected !== true
    );

    if (!availableVehicles) {
      return res.status(404).json({
        success: false,
        message: "No vehicles available at this location.",
      });
    }

    // If there is no next middleware after this one, send the response
    if (!req.route || !req.route.stack || req.route.stack.length === 1) {
      return res.status(200).json({
        success: true,
        data: availableVehicles,
      });
    }

    // If there is a next middleware, pass control to it
    res.locals.actionResult = [availableVehicles, model];
    next();
  } catch (error) {
    console.error("Get vehicles without booking error:", error);
    return next(
      errorHandler(500, "An error occurred while fetching available vehicles.")
    );
  }
};

// getting all variants of a model which are not booked
export const showAllVariants = async (req, res, next) => {
  try {
    const actionResult = res.locals.actionResult;
    const model = actionResult[1];

    if (!actionResult[0]) {
      return next(errorHandler(404, "no actionResult"));
    }
    const allVariants = actionResult[0].filter((cur) => {
      return cur.model === model;
    });

    res.status(200).json(allVariants);
  } catch (error) {
    next(errorHandler(500, "internal error in showAllVariants"));
  }
};

// show one representative of each model type available
export const showOneofkind = async (req, res, next) => {
  try {
    const actionResult = res.locals.actionResult;

    const modelsMap = {};
    const singleVehicleofModel = [];

    if (!actionResult) {
      return next(errorHandler(404, "no actionResult"));
    }

    actionResult[0].forEach((cur) => {
      if (!modelsMap[cur.model]) {
        modelsMap[cur.model] = true;
        singleVehicleofModel.push(cur);
      }
    });

    if (!singleVehicleofModel) {
      return next(errorHandler(404, "no vehicles available"));
    }

    res.status(200).json(singleVehicleofModel);
  } catch (error) {
    console.error("Show one of kind error:", error);
    next(errorHandler(500, "error in showOneofkind"));
  }
};

// filtering vehicles
export const filterVehicles = async (req, res, next) => {
  try {
    if (!req.body) {
      return next(errorHandler(401, "bad request no body"));
    }
    const transformedData = req.body;
    if (!transformedData) {
      return next(errorHandler(401, "select filter option first"));
    }
    const generateMatchStage = (data) => {
      const carTypes = [];
      data.forEach((cur) => {
        if (cur.type === "car_type") {
          const firstKey = Object.keys(cur).find((key) => key !== "type");
          if (firstKey) {
            carTypes.push(firstKey);
          }
        }
      });

      const transmissions = [];
      data.forEach((cur) => {
        if (cur.type === "transmition") {
          Object.keys(cur).forEach((key) => {
            if (key !== "type" && cur[key]) {
              transmissions.push(key);
            }
          });
        }
      });

      return {
        $match: {
          $and: [
            carTypes.length > 0 ? { car_type: { $in: carTypes } } : null,
            transmissions.length > 0
              ? { transmition: { $in: transmissions } }
              : null,
          ].filter((condition) => condition !== null),
        },
      };
    };

    const matchStage = generateMatchStage(transformedData);

    const filteredVehicles = await Vehicle.aggregate([matchStage]);
    if (!filteredVehicles) {
      return next(errorHandler(401, "no vehicles found"));
    }
    res.status(200).json({
      status: "success",
      data: {
        filteredVehicles,
      },
    });
  } catch (error) {
    console.error("Filter vehicles error:", error);
    next(errorHandler(500, "internal server error in filterVehicles"));
  }
};

// api to get the latest booking details
export const latestbookings = async (req, res, next) => {
  try {
    const { user_id } = req.body;
    const convertedUserId = new mongoose.Types.ObjectId(user_id);

    const bookings = await Booking.aggregate([
      {
        $match: {
          userId: convertedUserId,
        },
      },
      {
        $lookup: {
          from: "vehicles",
          localField: "vehicleId",
          foreignField: "_id",
          as: "result",
        },
      },
      {
        $project: {
          _id: 0,
          bookingDetails: "$$ROOT",
          vehicleDetails: {
            $arrayElemAt: ["$result", 0],
          },
        },
      },
      {
        $sort: {
          "bookingDetails.createdAt": -1,
        },
      },
      {
        $limit: 1,
      },
    ]);

    res.status(200).json(bookings);
  } catch (error) {
    console.error("Latest bookings error:", error);
    next(errorHandler(500, "internal server error in latestbookings"));
  }
};

// send booking details to user email
export const sendBookingDetailsEamil = (req, res, next) => {
  try {
    const { toEmail, data } = req.body;

    const transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: process.env.EMAIL_HOST,
        pass: process.env.EMAIL_PASSWORD,
      },
    });

    const generateEmailHtml = (bookingDetails, vehicleDetails) => {
      const pickupDate = new Date(bookingDetails.pickupDate);
      const dropOffDate = new Date(bookingDetails.dropOffDate);

      return `
          <div style="font-family: Arial, sans-serif; padding: 10px;">
              <h2>Booking Details</h2>
              <hr>
              <p><strong>Booking Id:</strong> ${bookingDetails._id}</p>
              <p><strong>Total Amount:</strong> ${bookingDetails.totalPrice}</p>
              <p><strong>Pickup Location:</strong> ${bookingDetails.pickUpLocation}</p>
              <p><strong>Pickup Date:</strong> ${pickupDate.getDate()}/${pickupDate.getMonth() + 1}/${pickupDate.getFullYear()} ${pickupDate.getHours()}:${pickupDate.getMinutes()}</p>
              <p><strong>Dropoff Location:</strong> ${bookingDetails.dropOffLocation}</p>
              <p><strong>Dropoff Date:</strong> ${dropOffDate.getDate()}/${dropOffDate.getMonth() + 1}/${dropOffDate.getFullYear()} ${dropOffDate.getHours()}:${dropOffDate.getMinutes()}</p>
              <h2>Vehicle Details</h2>
              <hr>
              <p><strong>Vehicle Number:</strong> ${vehicleDetails.registeration_number}</p>
              <p><strong>Model:</strong> ${vehicleDetails.model}</p>
              <p><strong>Company:</strong> ${vehicleDetails.company}</p>
              <p><strong>Vehicle Type:</strong> ${vehicleDetails.car_type}</p>
              <p><strong>Seats:</strong> ${vehicleDetails.seats}</p>
              <p><strong>Fuel Type:</strong> ${vehicleDetails.fuel_type}</p>
              <p><strong>Transmission:</strong> ${vehicleDetails.transmition}</p>
              <p><strong>Manufacturing Year:</strong> ${vehicleDetails.year_made}</p>
          </div>
      `;
    };

    const mailOptions = {
      from: process.env.EMAIL_HOST,
      to: toEmail,
      subject: "VeloGo Booking Details",
      html: generateEmailHtml(data[0].bookingDetails, data[0].vehicleDetails),
    };

    transporter.sendMail(mailOptions, function (error, info) {
      if (error) {
        console.error("Email send error:", error);
        next(errorHandler(500, "Failed to send email"));
      } else {
        console.log("Email sent: " + info.response);
        res.status(200).json("Email sent successfully");
      }
    });
  } catch (error) {
    console.error("Send booking email error:", error);
    next(error);
  }
};

export const findBookingsOfUser = async (req, res, next) => {
  try {
    if (!req.body) {
      return next(errorHandler(409, "_id of user is required"));
    }
    const { userId } = req.body;
    const convertedUserId = new mongoose.Types.ObjectId(userId);

    const bookings = await Booking.aggregate([
      {
        $match: {
          userId: convertedUserId,
        },
      },
      {
        $lookup: {
          from: "vehicles",
          localField: "vehicleId",
          foreignField: "_id",
          as: "result",
        },
      },
      {
        $project: {
          _id: 0,
          bookingDetails: "$$ROOT",
          vehicleDetails: {
            $arrayElemAt: ["$result", 0],
          },
        },
      },
    ]);

    res.status(200).json(bookings);
  } catch (error) {
    console.error("Find bookings of user error:", error);
    next(errorHandler(500, "internal error in findBookingOfUser"));
  }
};

