let id = Math.random;
const PrintMessage = (msg) => {
    if (!msg) return;
    let page = document.querySelector("#page");
    const div = document.createElement("div");
    div.className = "alert alert-success mt-2";
    div.id = "messageBoard" + id;
    div.innerText = msg;
    page.appendChild(div);
    setTimeout(() => document.getElementById("errorBoard" + id).remove(), 5000);
  };
  
  export default PrintMessage;
  