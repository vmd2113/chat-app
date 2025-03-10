import {useState} from 'react'
import './App.css'

function App() {
    const [count, setCount] = useState(0)

    return (
        <>
            <div className="min-h-screen bg-gray-100 flex items-center justify-center">
                <h5>Xin chào</h5>
            </div>
        </>
    )
}

export default App
