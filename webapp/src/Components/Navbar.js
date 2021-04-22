let navBar = document.querySelector(".navigationbar");
import callAPI from "../utils/api.js";
import { removeSessionData, currentUser, resetCurrentUser, getUserSessionData } from "../utils/session.js";
import PrintError from "./PrintError.js";
import PrintMessage from "./PrintMessage.js";
import { RedirectUrl } from "./Router.js";
let userData = getUserSessionData;
let idFurniture = 1;
let mapPhotos = new Map();
let typesOfFurniture;
// destructuring assignment
const Navbar = async () => {
  let nb;
  let user = currentUser;

  if (user) {
    
    nb = `
    <h1 class="lines" ></h1>
        <div class= "title">
          <a id="home" href="#">
            <img class="rect-logo" src="assets/rectangle.svg" alt="rectangle logo">
            <img class="logo-writing" src="assets/lvs.svg" alt="logo">
          </a>
        </div>
        <button id="all-furnitures-navbar" class="condensed small-caps">
          Voir tous les meubles
        </button>
        <div id="category" class="dropdown">
          <span class=" dropdown-toggle condensed small-caps" type="button" id="dropdownMenuButton1" data-bs-toggle="dropdown" aria-expanded="false">
            Types de meuble
          </span>
          <ul class="dropdown-menu condensed" aria-labelledby="dropdownMenuButton1">
            <li><a class="dropdown-toggle-item" href="#">Type1</a></li>
            <li><a class="dropdown-toggle-item" href="#">Type2</a></li>
            <li><a class="dropdown-toggle-item" href="#">Type3</a></li>
          </ul>
        </div>
        <button id="btnIntroduceVisit" class="btn btn-dark btn-navbar condensed small-caps">
          Demander une visite
        </button>

        <div class="user-menu">
        
          <p class="text-user" id="bonjour">Bonjour,</p>
          
          <p id="username-menu" class="text-user">${user.username}</p>
            <div class="dropleft" id="head-menu">
              <i id="user" class="bi bi-person-circle dropdown-toggle-user"></i>
              <ul class="dropdown-menu dropdown-menu-left condensed" aria-labelledby="dropdownMenuButton1">
                <li><a class="dropdown-item" id="profile" href="#">Profile</a></li>
                <li><a class="dropdown-item" id="logout" href="#">Se déconnecter</a></li>
              </ul>
            </div>
            <div id="adminToolsIcon">          
            </div>
        </div>

        <div class="hover_bkgr_fricc" id="hover_bkgr_friccNavbar">
        <span class="helper"></span>
        <div>
            <div class="popupCloseButton" id="popupCloseButtonNavbar">&times;</div>
            <h2>Introduire une demande de visite</h2>
          <form id="formRequest" class="RequestVisitForm">
          <div>
            <h4>Plage horaire *: </h4><input type="text" id="timeSlot" name="timeSlot" placeholder="exemple : le lundi de 18h à 22h" required><br>
            <h4>Adresse : (uniquement si elle est différente de votre addresse)</h4>
          </div>
          <div>
            <input type="text" class="form-control input-card" id="street" name="street" placeholder="Rue">

            <input type="text" class="form-control input-card" id="number" name="number" placeholder="Numéro">
                  
            <input type="text" class="form-control input-card" id="unitnumber" name="unitnumber" placeholder="Boite">
                  
            <input type="text" class="form-control input-card" id="postcode" name="postcode" placeholder="Code Postal">
                  
            <input type="text" class="form-control input-card" id="city" name="city" placeholder="Commune">
                  
            <input type="text" class="form-control input-card" id="country" name="country" placeholder="Pays">
              
          </div><br>
          <h4>Meuble(s) *: </h4><br>
            <div id="eachFurniture">
              <div>
                ${idFurniture}. <textarea class="description" id="furniture${idFurniture}" name="furniture${idFurniture}" required></textarea> 
                <label for="files${idFurniture}" class="bi bi-upload"></label>
                <input id="files${idFurniture}" name="files${idFurniture}" class="images" type="file" multiple required>
                <div id="typeFurniture" class="dropdown">
                  <label for="type">Type du meuble : </label>
                  <div id="tousLesTypes${idFurniture}"></div>
                </div>
              </div>
            </div>
         
          <br>
          <button class="bi bi-plus-circle" id="btnPlus" type="button"></button>
          
          <br>
          <button class="btn btn-outline-success" id="introduceBtn" name="introduceBtn" type="submit">Confirmer</button>
          <button class="btn btn-outline-danger" id="cancelBtn" name="cancelBtn" type="button">Annuler</button>
          </form>

          <span class="btnClose"></span>
        </div>
    </div>
        `;
    navBar.innerHTML = nb;

    let introduceBtn = document.getElementById("introduceBtn");
    //introduceBtn.addEventListener("click", onIntroduceRequest);
    let formRequest = document.getElementById("formRequest");
    formRequest.addEventListener("submit", onIntroduceRequest);
    let cancelBtn = document.getElementById("cancelBtn");
    cancelBtn.addEventListener("click", onCloseVisit);
    document.getElementById("popupCloseButtonNavbar").addEventListener("click", onCloseVisit);
    
    //inputImage = document.getElementById("files"+idFurniture);
    //console.log(document.getElementById("files"+idFurniture).files);
    //inputImage.addEventListener("change", encodeImagetoBase64(document.getElementById("files"+idFurniture).files));

    let btnPlus = document.getElementById("btnPlus");
    btnPlus.addEventListener("click", onAddFurniture);

    try {
      typesOfFurniture = await callAPI(
        "/api/furnitures/typeOfFurnitureList",
        "GET",
        undefined,
        undefined);
      onTypesOfFurniture(typesOfFurniture);
    } catch (err) {
      console.error("Navbar::onIntroduceVisit", err);
      PrintError(err);
    }

    if (user.role == "ADMIN") {
      let adminTools = document.getElementById("adminToolsIcon");
      adminTools.innerHTML = `<img src="../assets/key4Admin.png" alt="key" id="keyAdmin" width="30" height="30">`;
      //adminTools.innerHTML = `<i class="bi bi-key-fill" id="keyAdmin" width="30" height="30"></i>`;
      let keyAdmin = document.getElementById("keyAdmin");
      keyAdmin.addEventListener("click", onClickTools);
    }
    else {
      let userMenu = document.querySelector(".user-menu");
      userMenu.style.gridTemplateColumns = "25% 25% 25% 25%";
    }
  } else {
    nb = `<h1 class="lines" ></h1>
    <div class= "title">
      <a id="home" href="#">
        <img class="rect-logo" src="assets/rectangle.svg" alt="rectangle logo">
        <img class="logo-writing" src="assets/lvs.svg" alt="logo">
      </a>
    </div>
    <button id="all-furnitures-navbar" class="condensed small-caps">
        Voir tous les meubles
    </button>
    <div id="category" class="dropdown">
      <span class=" dropdown-toggle condensed small-caps" type="button" id="dropdownMenuButton1" data-bs-toggle="dropdown" aria-expanded="false">
        Types de meuble
      </span>
      <ul class="dropdown-menu condensed" aria-labelledby="dropdownMenuButton1">
        <li><a class="dropdown-toggle-item" href="#">Type1</a></li>
        <li><a class="dropdown-toggle-item" href="#">Type2</a></li>
        <li><a class="dropdown-toggle-item" href="#">Type3</a></li>
      </ul>
    </div>

    <a class="btn btn-dark btn-navbar condensed small-caps" href="#" data-uri="/login">S'identifier</a>`;
    navBar.innerHTML = nb;
  }

  if (user) {
    let logout = document.querySelector("#logout");
    logout.addEventListener("click", onLogout);

    let btnIntroduceVisit = document.getElementById("btnIntroduceVisit");
    btnIntroduceVisit.addEventListener("click", onIntroduceVisit);
  }
  let voir = document.getElementById("all-furnitures-navbar");
  voir.addEventListener("click", onFurnitureListPage);

  let home = document.getElementById("home");
  home.addEventListener("click", onHomePage);

};


const onFurnitureListPage = (e) => {
  e.preventDefault();
  RedirectUrl("/furnitures");
};
const onHomePage = (e) => {
  e.preventDefault();
  RedirectUrl("/");
};

const onLogout = (e) => {
  e.preventDefault();
  removeSessionData();
  resetCurrentUser();
  RedirectUrl("/");
  Navbar();
};

const onClickTools = (e) => {
  e.preventDefault();
  RedirectUrl("/visits")
}

const onIntroduceVisit = (e) => {
  e.preventDefault();
  document.getElementById("hover_bkgr_friccNavbar").style.display = "block";
}

const onCloseVisit = (e) => {
  e.preventDefault();
  document.getElementById("hover_bkgr_friccNavbar").style.display = "none";
}


const onTypesOfFurniture = (data) => {
  let eachType = `<select name="type${idFurniture}" id="type${idFurniture}" required>`;
  eachType += data
    .map((type) =>
      `<option value="${type.id}">${type.label}</option>`
    ).join("");
  let tousLesTypes = document.getElementById("tousLesTypes"+idFurniture);
  tousLesTypes.innerHTML = eachType;
  tousLesTypes.innerHTML += `</select>`
}


function encodeFile(file) {
  return new Promise((resolve, reject) => {
    var fileReader = new FileReader();
    fileReader.onload = function(fileLoadedEvent) {
      let base64 = fileLoadedEvent.target.result;
      resolve(base64);
    }
    fileReader.readAsDataURL(file)
  });
}

async function encodeFiles(files) { 
  const returnedFiles = {}
  if (files.length > 0) {
    for(let i = 0; i < files.length; i++) {
      returnedFiles[i] = await encodeFile(files[i])
    }
  }
  return returnedFiles; 
}

const onAddFurniture = (e) => {
  idFurniture++;
  console.log(idFurniture-1);
  let eachFurniture = document.getElementById("eachFurniture");
  let newFurniture= document.createElement('div');
  newFurniture.innerHTML = 
  `<br><br>
    <div>
      ${idFurniture}. <textarea class="description" id="furniture${idFurniture}" name="furniture${idFurniture}"></textarea> 
      <label for="files${idFurniture}" class="bi bi-upload"></label>
      <input id="files${idFurniture}" name="files${idFurniture}" class="images" type="file" multiple>
      <div id="typeFurniture${idFurniture}" class="dropdown">
        <label for="type">Type du meuble : </label>
        <div id="tousLesTypes${idFurniture}"></div>
      </div>
    <div>
  `;
  eachFurniture.appendChild(newFurniture);
  onTypesOfFurniture(typesOfFurniture);
}

const onIntroduceRequest = async (e) => {
  e.preventDefault();
  
  /*for (let i = 1; i<= idFurniture; i++){
    let files = document.getElementById("files"+idFurniture);
    console.log(files.value);
    encodeImagetoBase64(files.value, idFurniture)
  }*/
  
  let request = {
    timeSlot: e.target.elements.timeSlot.value,
    warehouseAddress: {
      street: e.target.elements.street.value,
      buildingNumber: e.target.elements.number.value,
      postCode: e.target.elements.postcode.value,
      country: e.target.elements.country.value,
      city: e.target.elements.city.value,
      unitNumber: e.target.elements.unitnumber?.value,
    },
    furnitures:{},

  };
  console.log(mapPhotos);
  for (let i = 1; i <= idFurniture; i++){
    let furniture = {
      id: i,
      description : e.target.elements['furniture'+i].value,
      idTypeOfFurniture: e.target.elements['type'+i].value,
      listPhotos: await encodeFiles(e.target.elements['files'+i].files),
    }
    request.furnitures[i] = furniture;
  }
  console.log(request);
  
  //en commentaire pour les tests
  try {
    const requestVisit = await callAPI(
      "/api/visits/introduce",
      "POST",
      userData.token,
      request
    );
    if (requestVisit) {
      RedirectUrl("/");
      PrintMessage("Votre demande de visite a bien été enregistrée. Elle est maintenant en attente de confirmation.");
    }
  } catch (err) {
    console.error("Navbar :: onIntroduceRequest", err);
    PrintError(err);
  }
}


export default Navbar;
