const personImg = new Image();
personImg.src = "man.png";
const personImgRev = new Image();
personImgRev.src = "manrev.png";

class Person {
  constructor(id, canvasH, canvasW, animationTime, pedestrian, initTopPersonY, numPositions, width, height) {
    this.id = id;
    this.y = initTopPersonY;
    this.animationTime = animationTime;
    this.canvasH = canvasH;
    this.canvasW = canvasW;
    this.block = canvasW / numPositions;
    this.width = width;
    this.height = height;

    this.position = pedestrian.position;
    this.isInNorth = false;
    this.x = this.position * this.block - this.width / 2;
    this.currentImg = personImg;
  }

  update(pedestrian) {
    this.isInNorth = pedestrian.isInNorth;
    this.position = pedestrian.position;
  }

  animate(ctx){
    let animating = false;
    if (this.x < this.position * (this.block + 1) - (this.width / 2) - 2){
      this.currentImg = personImg;
      this.x += Math.round((this.block + this.width / 2) / this.animationTime);
      animating = true;
    } else if (this.x > this.position * (this.block + 1) - (this.width / 2) + 2) {
      this.currentImg = personImgRev;
      this.x -= Math.round((this.block + this.width / 2) / this.animationTime);
      animating = true;
    }
    ctx.drawImage(this.currentImg, this.x, this.y, this.width, this.height);
    return animating;
  }

  updateAndAnimate(pedestrian, ctx){
    this.update(pedestrian);
    this.animate(ctx);
  }
}
