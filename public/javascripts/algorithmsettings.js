
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
  			.text("  Successfully updated settings")
		        .append("&nbsp; <i class='glyphicon glyphicon-ok-sign'></i>")
  			.removeClass("error")
  			.fadeIn()
  	},
  	error: () => {
  		$("#status")
  			.text("An error occoured")
  			.addClass("error")
  			.fadeIn()
  	},
  })
});
