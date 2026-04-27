function genres_id_change()
{
    var genreid = $("#genres_id").val();
    $.ajax({
        type: "GET",
        url: "http://localhost:8090/oscar-app/view/moviebygenres/"+genreid,
        data: '',
        cache: false,
        success: function(response)
        {
            $("#details").html(response);
        }
    });
}

function getAllGenres(){
	$.ajax({
        type: "GET",
        url: "http://localhost:8090/oscar-app/view/genre",
        data: '',
        cache: false,
        success: function(response)
        {
            $("#details").html(response);
        }
    });
}

function getAllMovies(){
	$.ajax({
        type: "GET",
        url: "http://localhost:8090/oscar-app/view/movie",
        data: '',
        cache: false,
        success: function(response)
        {
            $("#details").html(response);
        }
    });
	
}

function addGenres(){
	$.ajax({
        type: "GET",
        url: "http://localhost:8090/oscar-app/view/addgenre",
        data: '',
        cache: false,
        success: function(response)
        {
            $("#details").html(response);
        }
    });
}

function addMovie(){
	$.ajax({
        type: "GET",
        url: "http://localhost:8090/oscar-app/view/addmovie",
        data: '',
        cache: false,
        success: function(response)
        {
            $("#details").html(response);
        }
    });
}