import React from 'react'
import './SearchBox.css'

interface SearchBoxProps {
  placeholder: string
  value: string
  onChange: (value: string) => void
}

const SearchBox: React.FC<SearchBoxProps> = ({ placeholder, value, onChange }) => {
  return (
    <div className="search-box">
      <input
        type="text"
        className="search-input"
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
      <button className="search-button" onClick={() => {}}>
        Search
      </button>
    </div>
  )
}

export default SearchBox
