import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import PaymentInfoRequest from "../../models/PaymentInfoRequest";
import { SpinnerLoading } from "../Utils/SpinnerLoading";

export const PaymentPage = () => {
  const { user, isAuthenticated, getAccessTokenSilently } = useAuth0();
  const [httpError, setHttpError] = useState<string | null>(null);
  const [submitDisabled, setSubmitDisabled] = useState(false);
  const [fees, setFees] = useState(0);
  const [loadingFees, setLoadingFees] = useState(true);

  // Fetch pending fees from backend
  useEffect(() => {
    const fetchFees = async () => {
      if (isAuthenticated && user?.email) {
        try {
          const url = `${process.env.REACT_APP_API}/payment/search/findByUserEmail?userEmail=${user.email}`;

          const response = await fetch(url);
          if (!response.ok) {
            throw new Error("Error fetching fees!");
          }

          const data = await response.json();
          setFees(data.amount);
        } catch (err: any) {
          setHttpError("Something went wrong!");
        }
        setLoadingFees(false);
      }
    };

    fetchFees();
  }, [isAuthenticated, user]);

  // Razorpay checkout handler
  const checkout = async () => {
    if (!user?.email) return;

    setSubmitDisabled(true);

    try {
      const accessToken = await getAccessTokenSilently();

      // Prepare payment request
      let paymentInfo = new PaymentInfoRequest(
        Math.round(fees * 100),
        "INR",
        user.email
      );

      // Create Razorpay order
      const orderResponse = await fetch(
        `${process.env.REACT_APP_API}/payment/secure/create-order`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(paymentInfo),
        }
      );

      if (!orderResponse.ok) {
        throw new Error("Error creating Razorpay order");
      }

      const order = await orderResponse.json();

      // Razorpay popup options
      const options = {
        key: process.env.REACT_APP_RAZORPAY_KEY_ID,
        amount: order.amount,
        currency: order.currency,
        name: "Library App Fee Payment",
        description: "Payment for pending fees",
        order_id: order.id,
        handler: async function (response: any) {
          try {
            // Call payment-complete API
            const completeResponse = await fetch(
              `${process.env.REACT_APP_API}/payment/secure/payment-complete`,
              {
                method: "PUT",
                headers: {
                  Authorization: `Bearer ${accessToken}`,
                },
              }
            );

            if (!completeResponse.ok) {
              throw new Error("Payment completion failed");
            }

            setFees(0);
            alert("Payment successful!");
          } catch (err) {
            alert("Error completing payment");
          }
          setSubmitDisabled(false);
        },
        prefill: {
          email: user.email,
        },
        theme: {
          color: "#3399cc",
        },
      };

      // Open Razorpay
      const rzp = new (window as any).Razorpay(options);
      rzp.open();
    } catch (err: any) {
      console.error(err);
      setHttpError("Something went wrong!");
      setSubmitDisabled(false);
    }
  };

  // Loading spinner
  if (loadingFees) {
    return <SpinnerLoading />;
  }

  // Error message
  if (httpError) {
    return (
      <div className="container m-5">
        <p>{httpError}</p>
      </div>
    );
  }

  // UI return
  return (
    <div className="container">
      {fees > 0 && (
        <div className="card mt-3">
          <h5 className="card-header">
            Fees pending: <span className="text-danger">₹{fees}</span>
          </h5>
          <div className="card-body">
            <h5 className="card-title mb-3">Razorpay Payment</h5>

            <button
              disabled={submitDisabled}
              type="button"
              className="btn btn-md main-color text-white mt-3"
              onClick={checkout}
            >
              Pay fees
            </button>
          </div>
        </div>
      )}

      {fees === 0 && (
        <div className="mt-3">
          <h5>You have no fees!</h5>
          <Link type="button" className="btn main-color text-white" to="search">
            Explore top books
          </Link>
        </div>
      )}

      {submitDisabled && <SpinnerLoading />}
    </div>
  );
};
