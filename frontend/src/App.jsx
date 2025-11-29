import React, { useEffect, useState } from 'react'
import DatePicker from 'react-datepicker'
import 'react-datepicker/dist/react-datepicker.css'
import toast, { Toaster } from 'react-hot-toast'

const API = 'http://localhost:8080/api/appointments'

function toLocalISO(dt) {
  if (!dt) return ''
  const tzOffset = dt.getTimezoneOffset() * 60000
  return new Date(dt - tzOffset).toISOString().slice(0, 19)
}

function displayLocal(dtStr) {
  if (!dtStr) return 'Invalid Date'
  return dtStr.replace('T', ' ').slice(0, 16)
}

export default function App() {

  const [appointments, setAppointments] = useState([])
  const [customer, setCustomer] = useState('')
  const [vehicle, setVehicle] = useState('')
  const [mechanic, setMechanic] = useState('')
  const [start, setStart] = useState(null)
  const [end, setEnd] = useState(null)

  async function load() {
    try {
      const res = await fetch(API)
      const data = await res.json()
      setAppointments(data)
    } catch (e) {
      console.error(e)
    }
  }

  useEffect(() => { load() }, [])


  // ✅ FINAL BOOK FUNCTION WITH CUSTOM UI ERRORS
  async function book() {

    // Empty field validation
    if (!customer || !vehicle || !mechanic || !start || !end) {
      toast.error("Please fill all fields")
      return
    }

    // ⛔ User tries to schedule in the past
    if (start < new Date()) {
      toast.error("Cannot schedule in the past")
      return
    }

    // ⛔ End must be after start
    if (end <= start) {
      toast.error("End time must be after start time")
      return
    }

    const payload = {
      customerName: customer,
      vehicleReg: vehicle,
      mechanicId: Number(mechanic),
      startTime: toLocalISO(start),
      endTime: toLocalISO(end)
    }

    try {
      const res = await fetch(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })

      const txt = await res.text()

      if (res.ok) {
        toast.success("Appointment booked")
        setCustomer('')
        setVehicle('')
        setMechanic('')
        setStart(null)
        setEnd(null)
        load()
      } else {
        // Show backend details if they exist
        toast.error(txt || "Error booking appointment")
      }

    } catch (e) {
      console.error(e)
      toast.error("Error booking appointment")
    }
  }


  // ✅ FINAL CANCEL FUNCTION
  async function cancelAppointment(id) {
    if (!confirm("Cancel this appointment?")) return

    try {
      const res = await fetch(`${API}/${id}`, { method: "DELETE" })
      const txt = await res.text()

      if (res.ok) {
        toast.success("Appointment cancelled")
        load()
      } else {
        toast.error("Failed to cancel: " + (txt || res.status))
      }

    } catch (e) {
      console.error(e)
      toast.error("Failed to cancel: " + e.message)
    }
  }



  return (
    <div className="min-h-screen bg-slate-100 flex justify-center py-10 px-4">
      <Toaster />
      <div className="w-full max-w-md bg-white shadow-xl rounded-2xl overflow-hidden border border-slate-200">

        <div className="bg-blue-600 text-white px-6 py-5 flex items-center gap-3 rounded-b-xl">
          <span className="text-2xl">🍁</span>
          <h1 className="text-xl font-semibold">Maple Street</h1>
        </div>

        <div className="p-6">
          <h2 className="text-xl font-bold mb-4">Appointment Scheduler</h2>

          <p className="text-sm text-slate-600 mb-6">
            Fast booking for mechanics — avoids double-booking.
          </p>

          <h3 className="font-semibold mb-3">Book an appointment</h3>

          <div className="space-y-3">

            <input className="w-full border rounded-lg px-3 py-2"
              placeholder="Customer name"
              value={customer}
              onChange={e => setCustomer(e.target.value)}
            />

            <input className="w-full border rounded-lg px-3 py-2"
              placeholder="Vehicle reg"
              value={vehicle}
              onChange={e => setVehicle(e.target.value)}
            />

            <input className="w-full border rounded-lg px-3 py-2"
              placeholder="Mechanic ID (number)"
              value={mechanic}
              onChange={e => setMechanic(e.target.value)}
            />

            <div className="flex gap-2">
              <DatePicker
                selected={start}
                onChange={date => setStart(date)}
                showTimeSelect
                dateFormat="MM/dd/yyyy h:mm aa"
                placeholderText="Start"
                className="w-full border rounded-lg px-3 py-2"
              />

              <DatePicker
                selected={end}
                onChange={date => setEnd(date)}
                showTimeSelect
                dateFormat="MM/dd/yyyy h:mm aa"
                placeholderText="End"
                className="w-full border rounded-lg px-3 py-2"
                minDate={start}
              />
            </div>

            <div className="flex gap-2 pt-2">
              <button onClick={book}
                className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition">
                Book appointment
              </button>

              <button onClick={() => {
                setCustomer('')
                setVehicle('')
                setMechanic('')
                setStart(null)
                setEnd(null)
              }}
                className="px-4 bg-slate-200 rounded-lg hover:bg-slate-300 transition">
                Reset
              </button>
            </div>
          </div>


          <h3 className="font-semibold mt-8 mb-3">Upcoming appointments</h3>

          <div className="space-y-3">
            {appointments.map(a => (
              <div key={a.id} className="border rounded-xl p-4 bg-slate-50">

                <div className="font-semibold">
                  {a.customerName} — {a.vehicleReg}
                </div>

                <div className="text-sm text-slate-600">
                  Mechanic {a.mechanicId}
                </div>

                <div className="text-sm text-slate-700 mt-1">
                  {displayLocal(a.startTime)} → {displayLocal(a.endTime)}
                </div>

                <div className="mt-3 flex items-center gap-2">
                  <span className="inline-block px-2 py-1 text-xs rounded bg-green-200 text-green-700">
                    {a.status || 'SCHEDULED'}
                  </span>

                  <button
                    onClick={() => cancelAppointment(a.id)}
                    className="ml-auto bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded-full">
                    Cancel
                  </button>
                </div>

              </div>
            ))}
          </div>

        </div>
      </div>
    </div>
  )
}
