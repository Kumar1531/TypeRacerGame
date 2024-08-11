const quoteDisplayElement = document.getElementById('quoteDisplay')
const quoteInputElement = document.getElementById('quoteInput')
const timerElement = document.getElementById('timer')
const res = document.getElementById("res")
//const n = document.getElementById("xyz")
const rname =  localStorage.getItem('currentRoom');//document.getElementById("roomName")
const wpmDisplay = document.getElementById("wpm")
const user_name = sessionStorage.getItem('user')
//const progressContain = document.getElementById("progressContainer")

let now = -1;
let check = true;
let ws;
let textContent;
let startTime;
let totalSeconds;

const progressBarContainer = document.getElementById("progressBars");
const userProgressBars = new Map();

function init() {
	ws = new WebSocket("ws://localhost:8000/" + rname);
	
	console.log("Initiated"+rname)

	ws.onopen = function(event) {
		console.log("WebSocket connection established on room " + rname);
		ws.send(`JOIN: ${rname}`)
	};

	ws.onmessage = function(event) {
		console.log("Inside on message")
		const messages = document.getElementById("d");
		const message = event.data;
		console.log(message)
		if (message === "WAIT") {
			alert("The room is already in the game, please wait.....");
		}
		else if (message === "ROOM_FULL") {
            alert("The room is full. Please try another room.");
            history.back();
        }
		else if (message === "ENTER") {
			console.log("Waiting done")
			alert("You may now enter the game")
		}
		else if (message.startsWith("TEXT:")) {
			//alert("You may now enter the game")
			textContent = message.substring(5);
			document.getElementById('quoteDisplay').innerText = '';
			textContent.split('').forEach(character => {
				const characterSpan = document.createElement('span')
				characterSpan.innerText = character
				quoteDisplayElement.appendChild(characterSpan)
			})
			quoteInputElement.value = null
		}
		else if (message.startsWith("START")) {
			console.log("Start game ")
			startGame();
		}
		else if (message.startsWith("RESULT")) {
			messages.innerHTML += '<p>' + message + '</p>';
		}
		else if (message.startsWith("REMATCH")) {
			document.getElementById("rematchButton").style.display = "inline";
			document.getElementById("rematchButton").addEventListener('click', function() { location.reload() })
			document.getElementById("exitButton").style.display = "inline";
			// const user_name = "kumar"
			document.getElementById("exitButton").addEventListener('click', function() { history.back() })
		}
		else if (message.startsWith("PROGRESS:")) {
			const [_, user, progress, wpm] = message.split(":");
            updateProgressBar(user, progress, wpm);
		}
		

	};
}

function updateProgressBar(user, progress, wpm) {
    let progressBarWrapper;
    let progressBar;
    let wpmDisplay;

    if (userProgressBars.has(user)) {
        progressBarWrapper = userProgressBars.get(user);
        progressBar = progressBarWrapper.querySelector(".progressBar");
        wpmDisplay = progressBarWrapper.querySelector(".wpmDisplay");
    } else {
        progressBarWrapper = document.createElement("div");
        progressBarWrapper.classList.add("progressBarWrapper");

        progressBar = document.createElement("div");
        progressBar.classList.add("progressBar");

        wpmDisplay = document.createElement("div");
        wpmDisplay.classList.add("wpmDisplay");

        progressBarWrapper.appendChild(progressBar);
        progressBarWrapper.appendChild(wpmDisplay);
        progressBarContainer.appendChild(progressBarWrapper);

        userProgressBars.set(user, progressBarWrapper);
    }

    progressBar.style.width = progress + "%";
    wpmDisplay.textContent = `${wpm} WPM`;
}

function sendMessage() {
	const messageInput = `${n.value} finished check in ${now}seconds`;
	ws.send(messageInput);
}

quoteInputElement.addEventListener('input', () => {
	const arrayQuote = quoteDisplayElement.querySelectorAll('span')
	const arrayValue = quoteInputElement.value.split('')
	const inputText = document.getElementById("quoteInput").value;
	const inputLength = inputText.length;
	let correctChars = 0;

	for (let i = 0; i < inputLength; i++) {
		if (inputText[i] === textContent[i]) {
			correctChars++;
		} else {
			break;
		}
	}

	const progressPercentage = (correctChars / textContent.length) * 100;
	const now1 = new Date().getTime();
    const elapsed = (now1 - startTime) / 1000; 
    const wordsTyped = quoteInputElement.value.split(' ').length;
    const wpm = Math.round((wordsTyped / elapsed) * 60);
   // const user_name = "kumar"	
	ws.send(`PROGRESS:${user_name}:${progressPercentage}:${user_name} ${wpm}`);
	//document.getElementById("fillBar").style.width = progressPercentage + "%";
	
	let correct = true;
	
	arrayQuote.forEach((characterSpan, index) => {
		const character = arrayValue[index]
		if (character == null) {
			characterSpan.classList.remove('correct')
			characterSpan.classList.remove('incorrect')
			correct = false
		} else if (character === characterSpan.innerText) {
		
			characterSpan.classList.add('correct')
			characterSpan.classList.remove('incorrect')
			//ws.send(`PROGRESS: ${progressPercentage}`);
		} else {
			characterSpan.classList.remove('correct')
			characterSpan.classList.add('incorrect')
			correct = false
		}
	})

	if (correct || now > 60) {
		check = false;
		const now1 = new Date().getTime();
		totalSeconds = Math.floor((now1 - startTime) / 1000);
		const totalWords = quoteInputElement.value.split(' ').length;
		console.log(totalSeconds)
		const wpm = Math.round((totalWords / totalSeconds) * 60);
		console.log(wpm)
		if (now > 60) {
			//const user_name = document.getElementById("user_name").value
			res.textContent = `RESULT:${user_name} ran out of time`;
			ws.send(`RESULT:${user_name} ran out of time`);
		}
		else {
			//const user_name = "kumar"
			res.textContent = `RESULT:${user_name}  finsihed in ${now} seconds with ${wpm}wpm`;
			ws.send(`RESULT:${user_name} finsihed in ${now} seconds with ${wpm}wpm`);
		}
		timerElement.textContent = "Finished";
		quoteInputElement.readOnly = true;
	}
	else {
		console.log("updatewpm is called")
		updateWPM();
	}
})

function updateWPM() {
	const input = document.getElementById('quoteInput').value;
	const now1 = new Date().getTime();
	console.log(now1)
	console.log(startTime)
	const elapsed = (now1 - startTime) / 1000; // seconds
	console.log(elapsed)
	const wordsTyped = input.split(' ').length;
	const wpm1 = Math.round((wordsTyped / elapsed) * 60);
	document.getElementById('wpm').innerText = 'WPM: ' + wpm1;
}

function startGame() {
	quoteInputElement.removeAttribute('readonly')
	sTime();
}

function startButtonClick() {
	ws.send("START")
}

function readyButtonClick() {
	ws.send("READY");
	console.log("ready button clicked")
	const element = document.getElementById('quoteInput');
	element.focus();
	document.getElementById('readyButton').disabled = true;
}

function rematchButtonClick() {
	location.reload();
}


function sTime() {

	function updateTimer() {
		now++;
		if (now > 60) {
			ws.send("RESULT: ran out of time");
			quoteInputElement.readOnly = true;
			check = false;
		}
		timerElement.textContent = now;

		if (!check) {
			clearInterval(tm);
			const now = new Date().getTime();
			totalSeconds = Math.floor((now - startTime) / 1000);
			console.log(totalSeconds)
			//document.getElementById("fillBar").style.width = "0";
		}

	}

	startTime = new Date().getTime();
	const tm = setInterval(updateTimer, 1000);
	document.getElementById('wpm').innerText = 'WPM: 0';
}


window.onload = init;