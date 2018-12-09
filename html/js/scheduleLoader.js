


//Fetches the url paramaters and sets them to the variable
var urlParams;
(window.onpopstate = function () {
    var match,
        pl     = /\+/g,  // Regex for replacing addition symbol with a space
        search = /([^&=]+)=?([^&]*)/g,
        decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
        query  = window.location.search.substring(1);

    urlParams = {};
    while (match = search.exec(query))
        urlParams[decode(match[1])] = decode(match[2]);
})();



var schedule;

var postReq = {}
function loadSchedule(){
    postReq["shareCode"] = urlParams["id"]
    console.log("JS of req:" + JSON.stringify(postReq))
    var xhr = new XMLHttpRequest();
    xhr.open("POST",organizer_getSchedule2,true);

    console.log("PostReq:" + JSON.stringify(postReq));

    xhr.send(JSON.stringify(postReq));

    xhr.onloadend=function() {
        console.log(xhr);
        console.log(xhr.request);
        if (xhr.readyState == XMLHttpRequest.DONE) {
            console.log ("XHR:" + xhr.responseText);
            ret = JSON.parse(result)
            console.log("ret")
            if(status == 200){
                schedule = ret["schedule"]
                return true;
            }
            else {
                console.log("could not retrieve schedule, got status" + status)
                return false;
            }
        } else {
            console.log("Could not get req")
            return false;
        }
    }
}

//**INITIALIZATION CODE**

foundSchedule = loadSchedule();
if(foundSchedule){
    console.log(foundSchedule)

    document.getElementById("name").innerText = schedule["name"];

    document.getElementById("weekDate").value = schedule["startDate"];
    //Populating an empty schedule so the updateSchedule function can fill it in

    var scheduleTable = document.getElementById("scheduleTable").getElementsByTagName('tbody')[0];

    var firstDay = schedule["days"][0]
    var duration = schedule["meetingDuration"];
    for(i = 0; i < firstDay["timeslots"].length; i++)
    {
        var row = scheduleTable.insertRow(i);
        timeCell = row.insertCell(0)

        timeCell.innerHTML = firstDay["timeslots"][i]["startTime"]

        for(j = 0; j < 5; j++){
            row.insertCell(j+1)
        }
    }

    console.log(scheduleTable)

    updateSchedule(schedule["startDate"], urlParams["view"])
}
else{
    //TODO: redirect to homepage and delete this alert
    alert("Invalid id")

    //TODO, make this the pre-signed url
    //window.location.href = "index.html";
}

function cellClick(x) {
  td = $(x.target).closest('td');
  cellText = td.text();
  cellIndex = td.index();
  rowIndex = td.parent().index();
  console.log("clicking row: "+rowIndex+" cell: "+cellIndex+" text: "+cellText);
  processCell(cellText, cellIndex, rowIndex);
}

function changeDate(value){
    console.log("input"+value)
    weekDate = document.getElementById("weekDate").value;
    date = new Date(weekDate);
    console.log(date);
    date.setDate(date.getDate()+parseInt(value,10)); //parse input text into int
    if (value != 0){
        date.setDate(date.getDate()+1); //need to add one for formatDate()
        document.getElementById("weekDate").value = formatDate(date);
    }
    updateSchedule(date, urlParams["view"]);
}

function returnDate(value){
    console.log("input"+value)
    weekDate = document.getElementById("weekDate").value;
    date = new Date(weekDate);
    console.log(date);
    date.setDate(date.getDate()+parseInt(value,10)+1); //parse input text into int
    console.log(formatDate(date));
    return formatDate(date);
}

function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
}

function showDayTime(cellIndex, rowIndex){
    //TODO get actual meeting time from JSON
    switch(cellIndex){
        case 1:
            day = "Monday";
            date = returnDate(0); //base reference
            break;
        case 2:
            day = "Tuesday";
            date = returnDate(1);
            break;
        case 3:
            day = "Wednesday";
            date = returnDate(2);
            break;
        case 4:
            day = "Thursday";
            date = returnDate(3);
            break;
        case 5:
            day = "Friday";
            date = returnDate(4);
            break;
        default:
            day = "Invalid day of week";
    }
    days = schedule["days"];
    for (i = 0; i < days.length; i++){
        if (days[i]["date"] == date){
            time = days[i]["timeslots"][rowIndex]["startTime"];
            break;
        }
    }
    return date+", "+day+" at "+time;
}

//takes in a start date, finds the appropriate sunday and populates the schedule
function updateSchedule(startDate, organizerView){

    startDate = new Date(startDate);

    console.log(startDate);

    var days = schedule["days"]
    var prevSunday = new Date(startDate.setHours(-24 * startDate.getDay()));
    console.log(startDate.getDay());
    console.log(prevSunday);

    var scheduleTable = document.getElementById("scheduleTable").getElementsByTagName('tbody')[0];
    //checking if each day exists in the schedule, if it doesn't we place a closed day
    //TODO refactor code, this is super ineffecient
    for(x = 1; x < 6; x++){
        var found = false;
        var index;
        for(y = 0; y < days.length; y++){

            var difference = Math.ceil((new Date(days[y]["date"]) - prevSunday)/(1000*60*60*24));
            console.log(difference)
            if(difference == x){
                index = y
                found = true;
                break;
            }
        }

        //scheduleTable.rows[y].cells[x].innerHTML = days[y]["date"];
        if(found){

            var timeslots = days[index]["timeslots"]
            for(y = 0; y < timeslots.length; y++){
                if(timeslots[y]["isClosed"]){
                    if(timeslots[y]["participantInfo"]){
                        if (organizerView == 1){
                            scheduleTable.rows[y].cells[x].innerHTML = 'Booked by:' + timeslots[y]["participantInfo"];
                        }
                        else {
                            scheduleTable.rows[y].cells[x].innerHTML = 'Taken';
                        }
                        scheduleTable.rows[y].cells[x].style.backgroundColor = '#ffff99';
                    }
                    else{
                        scheduleTable.rows[y].cells[x].innerHTML = 'Closed';
                        scheduleTable.rows[y].cells[x].style.backgroundColor = '#ff4d4d';
                    }

                }

                else{
                    scheduleTable.rows[y].cells[x].innerHTML = 'Open';
                    scheduleTable.rows[y].cells[x].style.backgroundColor = '#66ff99';
                }
            }

        }
        else{
            for(y = 0; y < days[0]["timeslots"].length; y++){
                scheduleTable.rows[y].cells[x].innerHTML = 'Not on schedule';
                scheduleTable.rows[y].cells[x].style.backgroundColor = "gray";
            }
        }
    }

}
