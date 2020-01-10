const personImg = new Image();
personImg.src = "man.png";
const personImgRev = new Image();
personImgRev.src = "manrev.png";

class Person {
  constructor(id, canvasH, canvasW, animationTime, pedestrian, initTopPersonY, initBottomPersonY, numPositions, width, height) {
    this.id = id;
    this.animationTime = animationTime;
    this.canvasH = canvasH;
    this.canvasW = canvasW;
    this.block = canvasW / numPositions;
    this.width = width;
    this.height = height;
    this.initBottomPersonY = initBottomPersonY;
    this.initTopPersonY = initTopPersonY;
    this.position = pedestrian.position;
    this.isInNorth = pedestrian.isInNorth;
    this.y = this.isInNorth ? initTopPersonY : initBottomPersonY;
    this.x = this.position * this.block + this.block / 2 - this.width / 2;
    this.currentImg = personImg;
    this.crosswalkLength = initBottomPersonY - initTopPersonY;
  }

  update(pedestrian) {
    this.isInNorth = pedestrian.isInNorth;
    this.position = pedestrian.position;
    this.isOnCrosswalk = pedestrian.isOnCrosswalk;
  }

  animate(ctx){
    let animating = false;
    if (this.isOnCrosswalk) {
      let toLeft = this.x > (this.canvasW / 2 + 3);
      let toRight = this.x < (this.canvasW / 2 - 3);
      if (toLeft || toRight) {
        if (toRight) {
          this.x += Math.round((this.block + this.width / 2) / this.animationTime);
          animating = true;
        }
        if (toLeft) {
          this.x -= Math.round((this.block + this.width / 2) / this.animationTime);
          animating = true;
        }
      }
    } else if (this.x < this.position * this.block + this.block / 2 - (this.width / 2) - 2){
      this.currentImg = personImg;
      this.x += Math.round((this.block + this.width / 2) / this.animationTime);
      animating = true;
    } else if (this.x > this.position * this.block + this.block / 2 - (this.width / 2) + 2) {
      this.currentImg = personImgRev;
      this.x -= Math.round((this.block + this.width / 2) / this.animationTime);
      animating = true;
    }
    if (this.isInNorth && this.y > this.initTopPersonY) {
      this.y -= Math.round(this.crosswalkLength / this.animationTime);
      animating = true;
    } else if (!this.isInNorth && this.y < this.initBottomPersonY) {
      this.y += Math.round(this.crosswalkLength / this.animationTime);
      animating = true;
    }
    ctx.drawImage(this.currentImg, this.x, this.y, this.width, this.height);
    return animating;
  }
}
