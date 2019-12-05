

$(document).ready(function(){
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
const numHouses = 6;
const sidewalkC = "#efe5b0";
const sidewalkH = 30;
const streetC = "#3da744";
const streetH = 125;
const trashC = "#000000";
const trashCanC = "#41484f";
const roadH = canvasH - 2 * streetH;
const truckH = (roadH*2)/3;
const truckImageRatio = 564/516;

let x = - truckH * truckImageRatio;
let y = 100;
var truckImg = new Image();
truckImg.src = "truck.png";
var truckRevImg = new Image();
truckRevImg.src = "truckRev.png";
animate();

function animate() {
  paintBackground(ctx);
  ctx.drawImage(truckRevImg, canvasW - x - truckH * truckImageRatio, y + 100, truckH * truckImageRatio, truckH);
  ctx.drawImage(truckImg, x, y, truckH * truckImageRatio, truckH);
  x += 2;
  if (x < canvasW + truckH * truckImageRatio) requestAnimationFrame(animate);
}

function paintBackground(ctx){
  ctx.fillStyle = "#656262";
  ctx.fillRect(0, 0, canvasW, canvasH);

  // painting seperation line
  ctx.fillStyle = "#c5c5c5";
  ctx.fillRect(0, canvasH / 2 -5, canvasW, 10);

  paintStreet(numHouses, 0, 0, canvasW, streetH, ctx, true);
  paintStreet(numHouses, 0, canvasH - streetH, canvasW, streetH, ctx, false);

  paintArrow(canvasW/numHouses - (arrowW*1.5) / 2, streetH + (canvasH - 2 * streetH) / 4 - arrowH / 2, arrowW, arrowH, false, ctx);
  paintArrow(canvasW - canvasW/numHouses - (arrowW*1.5) / 2, streetH + 3 * (canvasH - 2 * streetH) / 4 - arrowH / 2, arrowW, arrowH, true, ctx);

  paintCrossing(canvasW / 2 - crossingW / 2, streetH, crossingW, canvasH - 2 *streetH, ctx);
}

function paintStreet(houseNum, left, top, width, height, ctx, bottomSidewalk){
  ctx.fillStyle = streetC;
  ctx.fillRect(left, top, width, height);
  ctx.fillStyle = sidewalkC;
  if (bottomSidewalk) {
    ctx.fillRect(left , top + height - sidewalkH, width, sidewalkH);
  } else {
      ctx.fillRect(left, top, width, sidewalkH);
  }
  for (i = 0; i < houseNum; i++) {
      paintHouse((i * width)/(houseNum) + width/(2*(houseNum)) - houseW/2, top + 50 , houseW, houseH, true, ctx);
  }
}

function paintHouse(left, top, width, height, isFront, ctx) {
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
  ctx.fillStyle = trashC;
  ctx.beginPath();
  ctx.moveTo(left + width + 6, top + height / 2);
  ctx.lineTo(left + width + 8, top + height / 2 - 6);
  ctx.lineTo(left + width + 12, top + height / 2 - 10);
  ctx.lineTo(left + width + 3 + width / 4, top + height / 2 - 7);
  ctx.lineTo(left + (5 * width) / 4 + 4, top + height / 2);
  ctx.fill();
  if (isFront) {
    ctx.fillStyle = "#832d25";
    ctx.fillRect(left + width * 4 / 6, top + height / 2, width / 6, height / 2);
  }
}

function paintCrossing(left, top, width, height, ctx){
  let paintedHeight = 0;
  let nextH = crossingBlockH + 2 * crossingSpaceH;
  ctx.fillStyle = crossingC;
  for (i = 0; paintedHeight + nextH < height; i++) {
    ctx.fillRect(left, top + i*crossingBlockH + (i+1)*crossingSpaceH, width, crossingBlockH);
    paintedHeight += crossingBlockH + crossingSpaceH;
  }
}

function paintArrow(left, top, width, height, isLeft, ctx) {
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
