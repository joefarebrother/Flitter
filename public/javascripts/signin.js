$(".welcome").hide()

$("#button").click(()=>{
	document.cookie = "user=" +$("#user").val() + ";path=/;max-age=100000"
})