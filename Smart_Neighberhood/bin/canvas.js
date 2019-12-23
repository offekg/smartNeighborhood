$(document).ready(function(){
let stateIndex = 0;
let currentState;
let nextState;
let isNextUpdated;
let states;
let notFoundCount = 0;

const animationTime = 120;

const canvas = document.querySelector('canvas');
const canvasH = canvas.height;
const canvasW = canvas.width;
let opacity = 0;
let ctx = canvas.getContext('2d');

const numHouses = 4;
const block = canvasW / numHouses;
const streetH = canvasH * 0.27;
const streetC = "#3da744";
const roadH = canvasH - 2 * streetH;
const roadC = "#656262";
const houseH = streetH * 0.416;
const houseW = houseH * 1.2;
const houseC = "#fcfcfc";
const crossingSpaceH = roadH / 20;
const crossingW = crossingSpaceH * 8;
const crossingC = "#ffffff";
const roofC = "#bd3d24";
const sidewalkC = "#efe5b0";
const sidewalkH = streetH / 4;
const trashC = "black";
const trashCanC = "#c2c2c2";
const lightC = "#fff200";
const arrowC = "#ffffff";
const arrowH = roadH / 20;
const arrowW = arrowH * 4;
const crossingBlockH = 17;
const truckH = (roadH*2)/3;
const truckImageRatio = 564/516;

const personH = houseH * 1.5;
const personImageRatio = 530 / 852;
const personW = personH * personImageRatio;
const personImg = new Image();
personImg.src = "man.png";
const initTopPersonX = - personW;
const initTopPersonY = streetH - personH;
const finalBottomPersonX = canvasW;
const finalBottomPersonY = streetH + roadH - personH;
let personX = initTopPersonX;
let personY = initTopPersonY;

const initTopTruckX = - 3 * block / 4;
const initTopTruckY = streetH - roadH / 10;
const initBottomTruckX = canvasW + block / 4;
const initBottomTruckY = streetH + roadH / 2 - roadH / 10;
let topTruckX = Number.MAX_SAFE_INTEGER;
let topTruckY = initTopTruckY;
let bottomTruckX = Number.MIN_SAFE_INTEGER;
let bottomTruckY = initBottomTruckY;
var truckImg = new Image();
truckImg.src = "truck.png";
var truckRevImg = new Image();
truckRevImg.src = "truckRev.png";

debugger;
// getNextState();

function getNextState() {
  waitingResponse = true;
  let xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
     nextState = JSON.parse(this.responseText);
     nextState.system.garbageTruckSouth_location = 3 - nextState.system.garbageTruckSouth_location;
     // TODO: remove once API is complete.
     nextState.environment.isNight = false;
     nextState.environment. garbageCansSouth.reverse();
     isNextUpdated = true;
     waitingResponse = false;
     if (stateIndex == 0) {
       currentState = nextState;
       console.log(currentState);
       isNextUpdated = false;
       stateIndex++;
       animate();
     }
    }
  };
  xhttp.open("GET", "/api", true);
  xhttp.send();
}

function animate() {
  debugger;
  paintBackground();
  paintStreetLamps(false);
  let animatingTop = animateTopTruck();
  let animatingBottom = animateBottomTruck();
  let animatingPerson = animatePerson();
  if (!animatingPerson && !animatingTop && !animatingBottom) {
    if (isNextUpdated) {
      if (currentState.environment.sidewalkSouth) {
        personX = initTopPersonX;
        personY = initTopPersonY;
      }
      currentState = nextState;
      console.log(currentState);
      isNextUpdated = false;
      stateIndex++;
    } else {
      console.log("State not updated. Index:" + stateIndex);
      notFoundCount++;
      if (notFoundCount > 10) return;
    }
  }
  if (!isNextUpdated && !waitingResponse) {
    getNextState();
  }
  paintStreetLamps(true);
  requestAnimationFrame(animate);
}

function animateTopTruck() {
  let animating = false;
  ctx.drawImage(truckImg, topTruckX, topTruckY, truckH * truckImageRatio, truckH);
  if (topTruckX < block * currentState.system.garbageTruckNorth_location + block / 4) {
    topTruckX += block / animationTime;
    animating = true;
  } else if (currentState.system.garbageTruckNorth_location == 0
    && topTruckX > block * currentState.system.garbageTruckNorth_location + block / 2) {
    topTruckX = initTopTruckX;
  }
  if (currentState.system.isCleaningN && topTruckY > initTopTruckY - roadH / 10){
    topTruckY -= 1;
    animating = true;
  }
  if (!currentState.system.isCleaningN && topTruckY < initTopTruckY){
    topTruckY += 1;
    animating = true;
  }
  return animating;
}

function animateBottomTruck(){
  let animating = false;
  ctx.drawImage(truckRevImg, bottomTruckX, bottomTruckY, truckH * truckImageRatio, truckH);
  if (bottomTruckX > block * currentState.system.garbageTruckSouth_location + block / 4){
    bottomTruckX -= block / animationTime;
    animating = true;
  } else if (currentState.system.garbageTruckSouth_location == 3
    && bottomTruckX < block * currentState.system.garbageTruckSouth_location) {
    bottomTruckX = initBottomTruckX;
  }
  if (currentState.system.isCleaningS && bottomTruckY < initBottomTruckY + roadH / 10) {
    bottomTruckY += 1;
    animating = true;
  }
  if (!currentState.system.isCleaningS && bottomTruckY > initBottomTruckY) {
    bottomTruckY -= 1;
    animating = true;
  }
  return animating;
}

function animatePerson(){
  let animating = false;
  ctx.drawImage(personImg, personX, personY, personW, personH);
  if (currentState.environment.sidewalkNorth && personX < (canvasW / 2) - (personW / 2)){
    personX += (canvasW / 2 + personW / 2) / animationTime;
    animating = true;
  }
  if (currentState.environment.crossingCrosswalkNS && personY < finalBottomPersonY) {
    personY += roadH / animationTime;
    animating = true;
  }
  if (currentState.environment.sidewalkSouth && personX < finalBottomPersonX) {
    personX += (canvasW / 2 + personW / 2) / animationTime;
    animating = true;
  }
  return animating;
}

function paintBackground(){
  paintRoad();
  paintStreet(0, 0, canvasW, streetH, false);
  paintStreet(0, canvasH - streetH, canvasW, streetH, true);
  paintNight();
  if (opacity > 0.2) {
    for (i = 0; i < numHouses; i++) {
        paintHouseLight(block * i + block / 2 - houseW/2, 50 , houseW, houseH, lightC);
    }
  }
}

function paintNight() {
  if (currentState.environment.isNight  && opacity < 0.5) {
    opacity += 0.5 / animationTime;
  }
  if (!currentState.environment.isNight  && opacity > 0) {
    opacity -= 0.4 / animationTime;
  }
  if (opacity > 0) {
    ctx.globalAlpha = opacity;
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, canvasW, canvasH);
    ctx.globalAlpha = 1;
  }
}

function paintRoad() {
  ctx.fillStyle = roadC;
  ctx.fillRect(0, streetH, canvasW, canvasH - 2 *streetH);
  // painting seperation line
  ctx.fillStyle = "#c5c5c5";
  ctx.fillRect(0, canvasH / 2 -5, canvasW, 10);
  paintCrossing(canvasW / 2 - crossingW / 2, streetH, crossingW, canvasH - 2 *streetH);
  paintArrow(block - (arrowW*1.5) / 2, streetH + (canvasH - 2 * streetH) / 4 - arrowH / 2, arrowW, arrowH, false);
  paintArrow(canvasW - block - (arrowW*1.5) / 2, streetH + 3 * (canvasH - 2 * streetH) / 4 - arrowH / 2, arrowW, arrowH, true);
}

function paintStreet(left, top, width, height, isBottom){
  ctx.fillStyle = streetC;
  ctx.fillRect(left, top, width, height);
  ctx.fillStyle = sidewalkC;
  if (!isBottom) {
    ctx.fillRect(left , top + height - sidewalkH, width, sidewalkH);
  } else {
      ctx.fillRect(left, top, width, sidewalkH);
  }
  for (i = 0; i < numHouses; i++) {
      paintHouse(i, isBottom, block * i + block / 2 - houseW/2, top + houseH , houseW, houseH);
  }
}

function paintStreetLamps(isBottom) {
  for (i = 1; i < numHouses; i++) {
    if (!isBottom) {
      paintStreetLamp(i * block, streetH - houseH - sidewalkH / 2, houseW / 3, houseH, false);
    }
    else {
      paintStreetLamp(i * block, canvasH - streetH - houseH + sidewalkH / 2, houseW / 3, houseH, true);
    }
  }
}

function paintStreetLamp(left, top, width, height, isBottom) {
  const lampW = width / 8;
  ctx.fillStyle = "black";
  ctx.beginPath();
  ctx.arc(left, top + height * 0.1, width / 2, Math.PI, 0);
  ctx.fill();
  ctx.fillRect(left -  lampW / 2, top + 5, lampW , height * 0.9);
  let isOn = isBottom ? currentState.system.lightSouth : currentState.system.lightNorth;
  if (isOn) {
    ctx.fillStyle = "yellow";
    ctx.globalAlpha = 0.7;
    ctx.beginPath();
    ctx.moveTo(left + lampW / 2 + 1, top + height * 0.1);
    ctx.lineTo(left + lampW / 2 + width / 2 - 3, top + height * 0.1);
    ctx.lineTo(left + lampW / 2 + 1 + width * 3 , top + height);
    ctx.lineTo(left + lampW / 2 + 1, top + height);
    ctx.fill();
    ctx.beginPath();
    ctx.moveTo(left - lampW / 2 - 1, top + height * 0.1);
    ctx.lineTo(left - lampW / 2 - width / 2 + 3, top + height * 0.1);
    ctx.lineTo(left - lampW / 2 - 1 - width * 3 , top + height);
    ctx.lineTo(left - lampW / 2 - 1, top + height);
    ctx.fill();
    ctx.globalAlpha = 1;
  }
}

function paintHouse(index, isBottom, left, top, width, height) {
  ctx.fillStyle = houseC;
  ctx.fillRect(left, top, width, height);
  ctx.fillStyle = roofC;
  ctx.beginPath();
  ctx.moveTo(left, top);
  ctx.lineTo(left+width/2, top-height * 0.6);
  ctx.lineTo(left+width, top);
  ctx.fill();
  ctx.fillStyle = trashCanC;
  const trashCanW = width / 2;
  const trashCanH = height / 2;
  const trashH = height / 4;
  const trashOffset = width / 5;
  ctx.fillRect(left + width + trashOffset - 1, top + height  - trashCanH, trashCanW, trashCanH);
  let hasGarbage = isBottom ? currentState.environment.garbageCansSouth[index] :
                              currentState.environment.garbageCansNorth[index];
  if (hasGarbage) {
    ctx.fillStyle = trashC;
    ctx.beginPath();
    ctx.moveTo(left + width + trashOffset, top + trashCanH);
    ctx.lineTo(left + width + trashOffset + trashCanW / 8, top + trashCanH - trashH *  2 / 3);
    ctx.lineTo(left + width + trashOffset + trashCanW * 1 / 3, top + trashCanH - trashH);
    ctx.lineTo(left + width + trashOffset + trashCanW * 2 / 3, top + trashCanH - trashH * 4 / 5);
    ctx.lineTo(left + width + trashOffset - 2 + trashCanW, top + trashCanH);
    ctx.fill();
  }

  if (!isBottom) {
    ctx.fillStyle = "brown";
    ctx.fillRect(left + width * 4 / 6, top + height / 2, width / 6, height / 2);
    paintHouseLight(left, top, width, height, "lightblue");
  }
}

function paintHouseLight(left, top, width, height, color) {
  ctx.fillStyle = color;
  ctx.fillRect(left + width * 1 / 6, top + height / 4, width / 5, height / 3);
}

function paintCrossing(left, top, width, height){
  let paintedHeight = 0;
  let nextH = crossingBlockH + 2 * crossingSpaceH;
  ctx.fillStyle = crossingC;
  for (i = 0; paintedHeight + nextH < height; i++) {
    ctx.fillRect(left, top + i*crossingBlockH + (i+1)*crossingSpaceH, width, crossingBlockH);
    paintedHeight += crossingBlockH + crossingSpaceH;
  }
}

function paintArrow(left, top, width, height, isLeft) {
  ctx.fillStyle = arrowC;
  if (isLeft){
    ctx.fillRect(left + width / 2, top, width, height);
    ctx.beginPath();
    ctx.moveTo(left + width / 2, top - height);
    ctx.lineTo(left, top + height / 2);
    ctx.lineTo(left + width / 2, top + height + height);
    ctx.fill();
  } else {
    ctx.fillRect(left, top, width, height);
    ctx.beginPath();
    ctx.moveTo(left + width, top - height);
    ctx.lineTo(left + width + width / 2, top + height / 2);
    ctx.lineTo(left + width, top + height + height);
    ctx.fill();
  }
}
});
