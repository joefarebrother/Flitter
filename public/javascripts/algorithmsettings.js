
$("#go").click(()=>{
	var data = {}
	$(".slider").map((i, s) => {
		data[$(s).attr("id")] = $(s).val() / $(s).attr("max")
	})

  $.ajax({
  	url: "api/setAlgSettings",
  	data: data,
  	success: () => {
  		$("#status")
  			.text("Successfully updated settings")	
  			.css("background", "#53c68c")
  			.fadeIn()
  	},
  	error: () => {
  		$("#status")
  			.text("An error occoured")
  			.css("background", "#ff3333")
  			.fadeIn()
  	},
  })
});
