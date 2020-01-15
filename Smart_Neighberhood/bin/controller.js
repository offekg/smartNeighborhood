function postHttpReqest(url, content, callback = function(){}) {
  let xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = callback;
  xhttp.open("POST", url, true);
  xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  console.log("sending to url: " + url + " content: " + content);
  xhttp.send(content);
}

function postToApi(content, callback = function(){}) {
  postHttpReqest("/api", "data: " + content, callback);
}

function setGarbage(index, isBottom) {
  let parameter = isBottom ? "garbageCansSouth:" : "garbageCansNorth:"
  postToApi("{" + parameter + index + "}");
}

function startScenario(number) {
  postHttpReqest("api/scenario", "data: {scenario:" + number + "}");
}

function updateButtons(mode) {
  $("#" + mode).addClass("active");
  $("#" + mode + " input").attr('checked', 'checked');
  if (mode === "AUTOMATIC") {
    $("button").not('#dropdownMenuButton').attr("disabled", true);
  }
}


function setMode(mode) {
  callback = function() {
    if (this.readyState == 4 && this.status == 200) {
      console.log(this.responseText);
      updateButtons(this.resetAnimation);
    }
  };
  postToApi("{mode:" + mode + "}",callback);
}

function sendPedestrian() {
  postToApi("{pedestrian=true}");
}

function setNightDay(isNight) {
  let time = isNight ? "NIGHT" : "DAY";
  postToApi('{dayTime="' + time + '"}');
}

function setEnergySavingMode(isSaving) {
  postToApi('{energyEfficiencyMode=' + isSaving + '}');
}

$(document).ready(function(){
  let xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      console.log(this.responseText);
      updateButtons(this.responseText);
    }
  };
  xhttp.open("GET", "/api/current_mode", true);
  xhttp.send();
});
