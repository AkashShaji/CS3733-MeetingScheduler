//Fetches the url paramaters and sets them to the variable
var urlParams;
var scheduleLoaded;
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

var schedule; //The schedule
var timeOfClickedSlot; //the clicked slot
var isOrganizer; //Is the user an organizer

function loadSchedule(init){
    var postReq = {}
    if(urlParams["shareCode"]==null){
      postReq["shareCode"] = "";
      postReq["secretCode"] = urlParams["secretCode"];
      console.log("loading page as organizer");
    }
    else if(urlParams["secretCode"]==null){
      postReq["shareCode"] = urlParams["shareCode"];
      postReq["secretCode"] = "";
      console.log("loading page as participant");
    }
    else alert("should not have both secretCode and shareCode");

    console.log("JS of req:" + JSON.stringify(postReq))
    var xhr = new XMLHttpRequest();
    xhr.open("POST",organizer_getSchedule,true);

    console.log("PostReq:" + JSON.stringify(postReq));

    xhr.send(JSON.stringify(postReq));

    xhr.onloadend=function() {
        //console.log(xhr);
        var found;
        console.log(xhr.request);
        if (xhr.readyState == XMLHttpRequest.DONE) {
            //console.log ("XHR:" + xhr.responseText);
            ret = JSON.parse(xhr.responseText)
            console.log(ret)
            if(ret["httpCode"] == 200){
                schedule = ret["schedule"];
                console.log(schedule);
                found = true;
                if(urlParams["shareCode"]==null){
                    console.log(schedule["shareCode"]);
                    document.getElementById("shareCodeDisplay").innerHTML = "Participant access code: " + schedule["shareCode"];
                }
            }
            else {
                console.log("could not retrieve schedule, got status" + status)
                found = false;
            }
        } else {
            console.log("Could not get req")
            found = false;
        }
        if(init){
            initSchedule(found);
        }
        else{
            updateSchedule(document.getElementById("weekDate").value , isOrganizer);
        }
    }
}

window.onload = function(){
  loadSchedule(true);
}

//**INITIALIZATION CODE**
function initSchedule(foundSchedule){
    if(foundSchedule){

        document.getElementById("name").innerText = schedule["scheduleName"];

        console.log("Start date:" + schedule["startDateStr"])
        document.getElementById("weekDate").value = schedule["startDateStr"];
        //Populating an empty schedule so the updateSchedule function can fill it in

        var scheduleTable = document.getElementById("scheduleTable").getElementsByTagName('tbody')[0];

        var firstDay = schedule["days"][0]
        var duration = schedule["meetingDuration"];
        for(i = 0; i < firstDay["timeSlots"].length; i++)
        {
            var row = scheduleTable.insertRow(i);
            timeCell = row.insertCell(0)

            timeCell.innerHTML = firstDay["timeSlots"][i]["startTime"]

            for(j = 0; j < 5; j++){
                row.insertCell(j+1)
            }
        }
        console.log(scheduleTable)
        updateSchedule(schedule["startDateStr"], isOrganizer)
        if(isOrganizer){loadSelect();}
    }
    else{
        alert("Invalid id")
        window.location.href = indexWebsite;
    }
}



var table = $('#scheduleTable');
table.on("click", "td", cellClick); //attaches the handler on the whole table, but filter the events by the "td" selector
function cellClick(x) {
    td = $(x.target).closest('td');
    cellText = td.text();
    cellIndex = td.index();
    rowIndex = td.parent().index();
    console.log("clicking row: "+rowIndex+" cell: "+cellIndex+" text: "+cellText);
    processCell(cellText, cellIndex, rowIndex);
}

function changeDate(value){
    console.log("New date:"+value)
    weekDate = document.getElementById("weekDate").value;
    date = new Date(weekDate);
    console.log(date);
    date.setDate(date.getDate()+parseInt(value,10)); //parse input text into int
    if (value != 0){
        date.setDate(date.getDate()+1); //need to add one for formatDate()
        document.getElementById("weekDate").value = formatDate(date);
    }
    updateSchedule(date, isOrganizer);
}

function returnDate(value){
    console.log("input"+value)
    weekDate =new Date(document.getElementById("weekDate").value);
    prevSunday = new Date(weekDate.setHours(-24 * weekDate.getDay()));
    date = new Date(prevSunday);
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

function showDayTime(cellIndex, rowIndex, option){
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
        if (days[i]["dateStr"] == date){
            timeOfClickedSlot = days[i]["timeSlots"][rowIndex]["startTime"];
            dayOfClickedSlot = days[i]["dateStr"];
            break;
        }
    }
    if(option==false){
      return date+", "+day+" at "+timeOfClickedSlot;
    }
    else {
      return date+timeOfClickedSlot;
    }

}


//takes in a start date, finds the appropriate sunday and populates the schedule
function updateSchedule(startDate){



    startDate = new Date(startDate);
    console.log("New Start Date:" + startDate);

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
            //console.log(days[y]["dateStr"])
            var difference = Math.ceil((new Date(days[y]["dateStr"]) - prevSunday)/(1000*60*60*24));
            //console.log(difference)
            if(difference == x){
                index = y
                found = true;
                break;
            }
        }

        //scheduleTable.rows[y].cells[x].innerHTML = days[y]["date"];
        if(found){

            var timeSlots = days[index]["timeSlots"]
            for(y = 0; y < timeSlots.length; y++){
                if(!timeSlots[y]["available"]){
                    if(timeSlots[y]["participantInfo"]){
                            scheduleTable.rows[y].cells[x].innerHTML = 'Booked by:' + timeSlots[y]["participantInfo"];
                        scheduleTable.rows[y].cells[x].style.backgroundColor = '#ffff99';
                    }
                    else{
                        scheduleTable.rows[y].cells[x].innerHTML = 'Closed';
                        scheduleTable.rows[y].cells[x].style.backgroundColor = '#ff4d4d';
                    }

                }

                else{
                    scheduleTable.rows[y].cells[x].innerHTML = 'Open'// + days[index]["dateStr"];
                    scheduleTable.rows[y].cells[x].style.backgroundColor = '#66ff99';
                }
            }

        }
        else{
            for(y = 0; y < days[0]["timeSlots"].length; y++){
                scheduleTable.rows[y].cells[x].innerHTML = 'Not on schedule';
                scheduleTable.rows[y].cells[x].style.backgroundColor = "gray";
            }
        }
    }

}
