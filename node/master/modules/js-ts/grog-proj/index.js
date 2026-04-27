require('dotenv').config();
const { ChatGroq } = require("@langchain/groq");

const model = new ChatGroq({
  model: "llama-3.3-70b-versatile",
  temperature: 0,
  apiKey: process.env.GROQ_API_KEY,
});

async function main() {
  const response = await model.invoke("Hello, how are you?");
  console.log(response.content);
}

main();