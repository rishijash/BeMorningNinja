/*
Author: Rushi Jash
Date: 09/02/2019
Notes:
    - Basic Alarm Clock by Ivan Rocha: https://github.com/theitrain/clock
    - Color palette adapted from color.adobe.com project "The Clock Strikes Nine" by user nicolagilroy ( https://color.adobe.com/The-Clock-Strikes-Nine-color-theme-1294130/ )
    - "Alarm Clock" font from dafont.com, created by David J. Patterson ( http://www.dafont.com/alarm-clock.font )
 */

document.addEventListener("DOMContentLoaded", function() {

    // Selectors
    var wrapper = document.getElementById('wrapper');
    var alarmHours = document.getElementById('alarm_hours');
    var alarmMinutes = document.getElementById('alarm_minutes');
    var alarmName = document.getElementById('alarm_name');
    var alarmAMPM = document.getElementById('alarm_ampm');
    var submit = document.getElementById('submit');
    var alarmMessage = document.getElementById('alarm_message');
    var alarmList = document.getElementById('alarm_list');

    var triggered = false;

    // Create array to store alarm times in
    var alarms = [];

    // Create alarm elements
    createAlarmElements();

    // Render initial time, then update every second after
    renderTime();
    setInterval(renderTime, 1000);

    // Set event listener for adding alarms
    addListenerForInput();

    // Alarm constructor
    function Alarm(timeArg, nameArg){
        this.time = timeArg;
        this.name = nameArg;

        this.getTime = function(){
            return this.time;
        }

        this.getName = function(){
            return this.name;
        }
    }

    // Functions
    function createAlarmElements(){

        for (i=1; i <= 12; i++){
            var hourOption = document.createElement('option');
            hourOption.innerHTML = '<option value="' + i + '">' + i + '</option>';
            alarmHours.appendChild(hourOption);
        }

        for (i=0; i < 60; i++){

            // Pad minutes
            var output;
            if (i < 10){
                output = '0' + i;
            }
            else{
                output = i;
            }

            var minuteOption = document.createElement('option');
            minuteOption.innerHTML = '<option value="' + output + '">' + output + '</option>';
            alarmMinutes.appendChild(minuteOption);
        }
    }

    // Render time
    function renderTime(){

        // create new date object
        var time = new Date();
        var ampm = 'AM';

        // Get values
        var timeSeconds = time.getSeconds();
        var timeMinutes = time.getMinutes();
        var timeHours = time.getHours();


        // Format values
        if (timeHours > 12){
            timeHours -= 12;
            ampm = 'PM'
        }

        if (timeMinutes < 10){
            timeMinutes = '0' + timeMinutes;
        }

        if (timeSeconds < 10){
            timeSeconds = '0' + timeSeconds;
        }

        // Display time
        wrapper.innerHTML = timeHours + ':' + timeMinutes + ':' + timeSeconds + ' ' + ampm;

        // Check if time matches alarm time

        if (alarms.length > 0){
            for (var i = 0; i < alarms.length; i++){
                var checkAlarm = alarms[i].getTime();

                if (checkAlarm.getHours() == timeHours && checkAlarm.getMinutes() == timeMinutes){
                    ringAlarm(alarms[i]);
                }
            }
        }


    }


    // Add a listener to the alarm input
    function addListenerForInput(){
        submit.addEventListener('click', setAlarm);
    }

    // Set alarm
    function setAlarm(){

        if (alarmName.value && alarmHours.value && alarmMinutes.value && alarmAMPM.value){

            var time = new Date();

            if (alarmAMPM.value == "PM"){
                time.setHours(parseInt(alarmHours.value) + 12);
            }
            else {
                time.setHours(alarmHours.value);
            }

            time.setMinutes(alarmMinutes.value);

            time.setSeconds(0);


            alert(time);
            // Create new alarm object and store it in alarms[]
            var newAlarm = new Alarm(time, alarmName.value);
            alarms.push(newAlarm);

            // Update alarm message text
            alarmMessage.innerHTML = "Your alarm";

            // Append date to list of dates in DOM
            var newListItem = document.createElement('li');
            newListItem.innerHTML = alarmName.value + " - " + time.toLocaleTimeString();
            alarmList.appendChild(newListItem);
        }
        else{
            alert('ERROR: valid time and name needed to set an alarm');
        }
    }

    // Do something when the alarm goes off
    function ringAlarm(alarmData){
        // get Instagram File for Username
        if(!triggered) {
            // get Video Link
            var url = getVideoLinkFromInstagram(alarmData.getName());

            // Play it
            window.open(urlStr,'_blank');
            triggered = true;
        }

    }

    function getVideoLinkFromInstagram(username) {
        var finalPostLink = "";
        var finalDisplayUrl = "";
        var finalSummary = "";
        var instagramUserProfileUrl = "https://www.instagram.com/" + username + "/?__a=1";
        var instagramJsonStr = httpGet(instagramUserProfileUrl);
        var instagramJson = JSON.parse(instagramJsonStr);
        var graphql = instagramJson.graphql;
        var user = graphql.user;
        var profile_pic_url_hd = user.profile_pic_url_hd;
        var biography = user.biography;
        var edge_felix_video_timeline = user.edge_felix_video_timeline;
        var edges = edge_felix_video_timeline.edges;
        for (i in edges) {
            edge = edges[i];
            var node = edge.node;
            var shortcode = node.shortcode;
            var display_url = node.display_url;
            var is_video = node.is_video;
            if(is_video == true) {
                finalPostLink = "https://www.instagram.com/p/" + shortcode + "/"
                break;
            }
        }
        var instagramPostUrl = finalPostLink;
        var instagramHtmlStr = httpGet(instagramPostUrl);
        var startIndex = instagramHtmlStr.indexOf("https://scontent.cdninstagram.com/v");
        var subStringHtml = instagramHtmlStr.substring(startIndex, instagramHtmlStr.length);
        var endIndex = subStringHtml.indexOf("\"");
        var urlStr = subStringHtml.substring(0, endIndex).replace("\\u0026", "&").replace("\\u0026", "&");
        return urlStr;
    }

    function httpGet(theUrl)
    {
      var xmlHttp = new XMLHttpRequest();
      xmlHttp.open( "GET", theUrl, false ); // false for synchronous request
      xmlHttp.send( null );
      return xmlHttp.responseText;
    }

});
