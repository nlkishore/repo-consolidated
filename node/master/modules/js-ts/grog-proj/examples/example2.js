//require('dotenv').config();
require('dotenv').config({ path: require('path').resolve(__dirname, '../.env') });
const { ChatGroq } = require("@langchain/groq");

const model = new ChatGroq({
  model: "llama-3.3-70b-versatile",
  temperature: 0,
  apiKey: process.env.GROQ_API_KEY,
});

(async () => {
  const response = await model.invoke([{ role: "user", content: "Hello" }]);
  console.log(response.content);
})();