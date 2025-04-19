const inputs = document.querySelectorAll(".input");


function addcl(){
	let parent = this.parentNode.parentNode;
	parent.classList.add("focus");
}

function showConfirmation(event, linkElement) {

	// Prevent the default link behavior for td links
	event.preventDefault();

	// Create the confirmation dialog
	var dialog = document.createElement("div");
	dialog.className = "confirm-dialog";
	dialog.innerHTML = `
        <p>Are you sure you want to delete this?(Once deleted it cannot be reverted)</p>
        <button class="ok">OK</button>
        <button class="cancel">Cancel</button>
    `;

	// Style the confirmation dialog
	dialog.style.position = "fixed";
	dialog.style.top = "50%";
	dialog.style.left = "50%";
	dialog.style.transform = "translate(-50%, -50%)";
	dialog.style.zIndex = "9999";

	// Attach event listeners to the buttons
	dialog.querySelector(".ok").addEventListener("click", async function() {
		await new Promise(resolve => setTimeout(resolve, 0));
		dialog.remove();
		window.location = linkElement.href; // Follow the link
	});

	dialog.querySelector(".cancel").addEventListener("click", function() {
		dialog.remove();
		return false;
	});

	// Append the dialog to the document body
	document.body.appendChild(dialog);

	return false; // Prevent default link behavior
}




function remcl(){
	let parent = this.parentNode.parentNode;
	if(this.value == ""){
		parent.classList.remove("focus");
	}
}

function w3_open() {
	document.getElementById("sidebar").style.display = "block";
	document.getElementById("margin-100-for-shifting").style.margin = "100px";
}

function w3_close() {
	document.getElementById("sidebar").style.display = "none";
	document.getElementById("margin-100-for-shifting").style.margin = "0";
}


inputs.forEach(input => {
	input.addEventListener("focus", addcl);
	input.addEventListener("blur", remcl);
});
