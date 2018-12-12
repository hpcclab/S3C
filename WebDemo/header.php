<html>

<head>
    <meta charset="utf-8">

    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Semantic Search Search over Secured Data in the Cloud</title>
    <!--    Icon -->
    <link rel="icon" href="images/icon.jpg">

    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">

    <!-- jQuery library -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>

    <!-- Latest compiled JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>

    <!--    Upload Javascript and CSS-->
    <link rel="stylesheet" type="text/css" href="http://www.shieldui.com/shared/components/latest/css/light-bootstrap/all.min.css" />
    <script type="text/javascript" src="http://www.shieldui.com/shared/components/latest/js/shieldui-all.min.js"></script>

    <!--    Extra CSS-->
    <link rel="stylesheet" href="css/extra.css">
    <link rel="stylesheet" href="css/extra_1.css">

    <script>
        $(document).ready(function(){
            $('[data-toggle="popover"]').popover();

            $("div.bhoechie-tab-menu>div.list-group>a").click(function(e) {
                e.preventDefault();
                $(this).siblings('a.active').removeClass("active");
                $(this).addClass("active");
                var index = $(this).index();
                $("div.bhoechie-tab>div.bhoechie-tab-content").removeClass("active");
                $("div.bhoechie-tab>div.bhoechie-tab-content").eq(index).addClass("active");
            });

            $('.glyphicon-remove').click(function (event) {
                console.log($(this).attr('id'));
                var form_id = 'removeForm' + $(this).attr('id');
                console.log(form_id);
                document.getElementById(form_id).onsubmit();
                $('form#'+form_id).submit();
            });


        });
        function somefunction(sth) {
            if(confirm('Are you sure to delete the file?'))
                document.getElementById(sth).submit();
//            else
//                alert('Sth');
        };
        function searchSubmit() {
          document.getElementById('searchForm').submit();
        };
    </script>
</head>

<body>
