

$(document).ready(function(){
let canvas = document.querySelector('canvas');
// canvas.style.width ='100%';
// canvas.style.height='100%';
// canvas.style.border = "1px solid black";
// canvas.width  = canvas.offsetWidth;
// canvas.height = canvas.offsetHeight;
let canvasH = canvas.height;
let canvasW = canvas.width;
let ctx = canvas.getContext('2d');
ctx.fillStyle = "#656262";
ctx.fillRect(0, 0, canvasW, canvasH);

// painting seperation line
ctx.fillStyle = "#c5c5c5";
ctx.fillRect(0, canvasH / 2 -5, canvasW, 10);

const numHouses = 4;
const streetH = 125;
paintStreet(numHouses, 0, 0, canvasW, streetH, ctx, true);
paintStreet(numHouses, 0, canvasH - streetH, canvasW, streetH, ctx, false);

const arrowW = 40;
const arrowH = 10;
paintArrow(canvasW/numHouses - (arrowW*1.5) / 2, streetH + (canvasH - 2 * streetH) / 4 - arrowH / 2, arrowW, arrowH, false, ctx);
paintArrow(canvasW - canvasW/numHouses - (arrowW*1.5) / 2, streetH + 3 * (canvasH - 2 * streetH) / 4 - arrowH / 2, arrowW, arrowH, true, ctx);

const crossingW = 80;
paintCrossing(canvasW / 2 - crossingW / 2, streetH, crossingW, canvasH - 2 *streetH, ctx);

});

function paintStreet(houseNum, left, top, width, height, ctx, bottomSidewalk){
  const streetC = "#3da744";
  const houseH = 52;
  const houseW = 62;
  ctx.fillStyle = streetC;
  ctx.fillRect(left, top, width, height);
  const sidewalkC = "#efe5b0";
  const sidewalkH = 30;
  ctx.fillStyle = sidewalkC;
  if (bottomSidewalk) {
    ctx.fillRect(left , top + height - sidewalkH, width, sidewalkH);
  } else {
      ctx.fillRect(left, top, width, sidewalkH);
  }
  for (i = 0; i < houseNum; i++) {
      paintHouse((i * width)/(houseNum) + width/(2*(houseNum)) - houseW/2, top + 50 , houseW, houseH, ctx);
  }
}

function paintHouse(left, top, width, height, ctx) {
  const houseC = "#fcfcfc";
  ctx.fillStyle = houseC;
  ctx.fillRect(left, top, width, height);
  ctx.fillStyle = "#832d25";
  ctx.beginPath();
  ctx.moveTo(left, top);
  ctx.lineTo(left+width/2, top-30);
  ctx.lineTo(left+width, top);
  ctx.fill();
  const trashCanC = "#41484f";
  ctx.fillStyle = trashCanC;
  ctx.fillRect(left + width + 5, top + height / 2, width / 4, height / 2);
  const trashC = "#000000";
  ctx.fillStyle = trashC;
  let random_boolean = Math.random() >= 0.5;
  if (random_boolean) {
    ctx.beginPath();
    ctx.moveTo(left + width + 6, top + height / 2);
    ctx.lineTo(left + width + 8, top + height / 2 - 6);
    ctx.lineTo(left + width + 12, top + height / 2 - 10);
    ctx.lineTo(left + width + 3 + width / 4, top + height / 2 - 7);
    ctx.lineTo(left + (5 * width) / 4 + 4, top + height / 2);
    ctx.fill();
  }
}

function paintCrossing(left, top, width, height, ctx){
  const spaceH = 10;
  const blockH = 17;
  let paintedHeight = 0;
  let nextH = blockH + 2 * spaceH;
  const crossingC = "#ffffff";
  ctx.fillStyle = crossingC;
  for (i = 0; paintedHeight + nextH < height; i++) {
    ctx.fillRect(left, top + i*blockH + (i+1)*spaceH, width, blockH);
    paintedHeight += blockH + spaceH;
  }
}

function paintArrow(left, top, width, height, isLeft, ctx) {
  const arrowC = "#ffffff";
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
