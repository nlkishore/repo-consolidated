$(document).on('change','#genresselect' ,function(){
  var val = $('#genresselect option:selected').val();
  var text = $('#genresselect option:selected').text();
  console.log(' value is '+val +' Text is '+text)
})