function postHttpReqest(url, content) {
  let xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      console.log("SUCCESS" + this.responseText);
    }
  };
  xhttp.open("POST", url, true);
  xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  console.log("sending to url: " + url + " content: " + content);
  xhttp.send(content);
}

function postToApi(content) {
  postHttpReqest("/api", "data: " + content);
}

function setGarbage(index, isBottom) {
  let parameter = isBottom ? "garbageCansSouth:" : "garbageCansNorth:"
  postToApi("{" + parameter + index + "}");
}

function startScenario(number) {
  postHttpReqest("api/scenario", "data: {scenario:" + number + "}");
}

function setMode(mode) {
  postToApi("{mode:" + mode + "}");
}

function sendPedestrian(){
  //TODO: implement
}
