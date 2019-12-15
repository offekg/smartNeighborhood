$(document).ready(function(){
let stateIndex = 0;
let states;
let params;

const arrowC = "#ffffff";
const arrowH = 10;
const arrowW = 40;
const canvas = document.querySelector('canvas');
const canvasH = canvas.height;
const canvasW = canvas.width;
const crossingBlockH = 17;
const crossingC = "#ffffff";
const crossingSpaceH = 10;
const crossingW = 80;
const ctx = canvas.getContext('2d');
const houseC = "#fcfcfc";
const houseH = 52;
const houseW = 62;
const numHouses = 4;
const sidewalkC = "#efe5b0";
const sidewalkH = 30;
const streetC = "#3da744";
const streetH = 125;
const trashC = "#000000";
const trashCanC = "#41484f";
const roadH = canvasH - 2 * streetH;
const truckH = (roadH*2)/3;
const truckImageRatio = 564/516;
const block = canvasW / numHouses;

const initTopTruckX = - truckH * truckImageRatio;
const initTopTruckY = streetH - 20;
const initBottomTruckX = canvasW;
const initBottomTruckY = streetH + roadH / 2 - 20;
let topTruckX = initTopTruckX;
let topTruckY = initTopTruckY;
let bottomTruckX = initBottomTruckX;
let bottomTruckY = initBottomTruckY;
var truckImg = new Image();
truckImg.src = "truck.png";
var truckRevImg = new Image();
truckRevImg.src = "truckRev.png";
getStates();

function getStates () {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
     console.log(JSON.parse(this.responseText));
     states = JSON.parse(this.responseText);
     params = states[stateIndex];
     paintBackground();
     animate();
    }
  };
  xhttp.open("GET", "/states.json", true);
  xhttp.send();
}

function animate() {
  paintRoad();
  paintStreet(0, 0, canvasW, streetH, false);
  paintStreet(0, canvasH - streetH, canvasW, streetH, true);
  let animatingTop = animateTopTruck();
  let animatingBottom = animateBottomTruck();
  if (!animatingTop && !animatingBottom && stateIndex < states.length - 1) {
    stateIndex++;
    params = states[stateIndex];
  }
  paintStreetLamps();
  requestAnimationFrame(animate);
}

function animateTopTruck() {
  let animating = false;
  ctx.drawImage(truckImg, topTruckX, topTruckY, truckH * truckImageRatio, truckH);
  if (topTruckX < block * params.truckTop + block / 4){
    topTruckX += 3;
    animating = true;
  }
  if (params.topIsCleaning && topTruckY > initTopTruckY - 30){
    topTruckY -= 1;
    animating = true;
  }
  if (!params.topIsCleaning && topTruckY < initTopTruckY){
    topTruckY += 1;
    animating = true;
  }
  return animating;
}

function animateBottomTruck(){
  let animating = false;
  ctx.drawImage(truckRevImg, bottomTruckX, bottomTruckY, truckH * truckImageRatio, truckH);
  if (bottomTruckX > block * params.truckBottom + block / 4){
    bottomTruckX -= 3;
    animating = true;
  }
  if (params.bottomIsCleaning && bottomTruckY < initBottomTruckY + 30) {
    bottomTruckY += 1;
    animating = true;
  }
  if (!params.bottomIsCleaning && bottomTruckY > initBottomTruckY) {
    bottomTruckY -= 1;
    animating = true;
  }
  return animating;
}

function paintBackground(){
  paintRoad();
  paintStreet(0, 0, canvasW, streetH, false);
  paintStreet(0, canvasH - streetH, canvasW, streetH, true);
}

function paintRoad() {
  ctx.fillStyle = "#656262";
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
      paintHouse(i, isBottom, block * i + block / 2 - houseW/2, top + 50 , houseW, houseH);
  }
}

function paintStreetLamps() {
  for (i = 0; i < numHouses / 2; i++) {
        paintStreetLamp((2*i + 1) *block, streetH - houseH - sidewalkH / 2, houseW / 3, houseH, false);
        paintStreetLamp((2*i + 1) *block, canvasH - streetH - houseH + sidewalkH / 2, houseW / 3, houseH, true);
  }
}

function paintStreetLamp(left, top, width, height, isBottom) {
  const lampW = width / 8;
  ctx.fillStyle = "black";
  ctx.beginPath();
  ctx.arc(left, top + height * 0.1, width / 2, Math.PI, 0);
  ctx.fill();
  ctx.fillRect(left -  lampW / 2, top + 5, lampW , height * 0.9);
  let isOn = isBottom ? params.lightBottm : params.lightTop;
  if (isOn) {
    // ctx.fillStyle = "rgb(255, 255, 0, 0.5)";
    ctx.fillStyle = "yellow";
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
  }
}

function paintHouse(index, isBottom, left, top, width, height) {
  ctx.fillStyle = houseC;
  ctx.fillRect(left, top, width, height);
  ctx.fillStyle = "#832d25";
  ctx.beginPath();
  ctx.moveTo(left, top);
  ctx.lineTo(left+width/2, top-30);
  ctx.lineTo(left+width, top);
  ctx.fill();
  ctx.fillStyle = trashCanC;
  ctx.fillRect(left + width + 5, top + height / 2, width / 4, height / 2);
  let hasGarbage = isBottom ? params.garbageBottom[index] : params.garbageTop[index];
  if (hasGarbage) {
    ctx.fillStyle = trashC;
    ctx.beginPath();
    ctx.moveTo(left + width + 6, top + height / 2);
    ctx.lineTo(left + width + 8, top + height / 2 - 6);
    ctx.lineTo(left + width + 12, top + height / 2 - 10);
    ctx.lineTo(left + width + 3 + width / 4, top + height / 2 - 7);
    ctx.lineTo(left + (5 * width) / 4 + 4, top + height / 2);
    ctx.fill();
  }
  if (!isBottom) {
    ctx.fillStyle = "brown";
    ctx.fillRect(left + width * 4 / 6, top + height / 2, width / 6, height / 2);
    ctx.fillStyle = params.isNight ? "yellow" : "lightblue";
    ctx.fillRect(left + width * 1 / 6, top + height / 4, width / 5, height / 3);
  }
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
