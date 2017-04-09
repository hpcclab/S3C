
<?php

//require_once("../java/Java.inc");

include("header.php");

?>
    <div class="container-custom">
    	<!-- <div class="row"> -->
            <!-- <div class="bhoechie-tab-container"> -->
            <div class="row">
                <div class="col-md-2 bhoechie-tab-menu">
                  <span id="startServer" class="col-xs-3 list-group-item text-center">

                    <form id="startServerForm" action="home.php" method="POST">
                      <a href="#" style="text-decoration: none;">
		      <h4 id="start_pause_icon" class="glyphicon glyphicon-play"></h4><br/>
                      <div id="status">Start</div>
		      </a> 
                      <input type="hidden" name="startServer" value="true">
                    </form>

                    <form id="stopServerForm" action="home.php" method="POST">
                      <input type="hidden" name="stopServer" value="true">
                    </form>

                  </span>
                  <div class="list-group">
                    <a href="#" class="col-xs-4 list-group-item active text-center" id="searchPanel">
                      <h4 class="glyphicon glyphicon-search"></h4><br/>Search
                    </a>
                    <a href="#" class="col-xs-5 list-group-item text-center" id="uploadPanel">
                      <h4 class="glyphicon glyphicon-upload"></h4><br/>Upload
                    </a>
                  </div>
                </div>
              </div>

              <?php
              $query_send_pre = 'ps aux | grep "java -jar SemanticSearchCloud.jar"';
              exec($query_send_pre, $output, $return);
              if(!$return){

                if(sizeof($output)>2){
//                    die(print_r($output));
                    ?>
                  <script>
                    document.getElementById("start_pause_icon").className = "glyphicon glyphicon-pause";
                    document.getElementById("status").innerHTML = "Pause";
                  </script>
                  <?php
                  // echo "<div class='col-mid-6'>Server is started. Please start your request!</div>";
                }
                else{
                  // echo "<div class='col-mid-6'>Server is already started. Please continue your request!</div>";
                }
              }
              else {
                echo "???";
              }
               ?>
              <div class="row">
                <div class="bhoechie-tab">
                  <div class="bhoechie-tab-content active" id="searchContentPanel">
                    <div class="col-sm-6">
      	               <img class="img-responsive center-block" src="images/logo-300x191.jpg" style="max-height:220px">
                  	</div>
                  	<div class="col-sm-6">
            		       <img class="img-responsive center-block" src="https://www.louisiana.edu/sites/louisiana/files/styles/large/public/news-images/2015-12/600by400logo.png?itok=r8YCOLPZ" style="max-height:220px">
                    </div>
                    <center>
                      <div class="col align-self-center">
                        <h1>S3C: Secured Semantic Search over Encrypted Data in the Cloud </h1>
                      </div>
                    </center>

                    <form action="home.php" method="POST" id="searchForm">
<!--                      <div class="field" id="searchform">-->
<!--                        <input type="text" id="searchterm" name="query" placeholder="what are you searching for?" />-->
<!--                          <button type="submit" id="search">Find!</button>-->
<!--                      </div>-->

                        <div class="search-box col-lg-6 col-lg-offset-4" id="SBox">
                            <input id="search" name="query" placeholder="<?php if(isset($_POST["query"])){ echo $_POST["query"];}else{ echo "Search...";}?>" type="text">
                            <div class="also search-link" onclick="searchSubmit();" id="searchclick"></div>
<!--                            <a class="fa fa-cog also setting"></a>-->
                        </div>
                    </form>
                    <?php
                      if($_POST["query"]){
                      $query = $_POST["query"];
                      //echo $query;
                      $query_send = "java -jar SemanticSearchClient.jar -s \"" . $query . "\" n";
                      //echo $query_send;
                      $length = 2*strlen($query) + 40;
                      exec($query_send, $output, $return);
                      $output1 = "This is a test and this is a test and this is a test and this is a test and this is a test";
                      //$out = substr($output, $length);
                      //print_r($output);
                      $start;
                      $bool = false;

                      echo '<div class="container">';
                          echo '<div class="row">';
                              echo '<table class="table">
                                  <thead>
                                  <tr>
                                      <th>Ranking</th>
                                      <th>Document</th>
                                      <th>Details</th>
                                      <th>Operation</th>
                                  </tr>
                                  </thead>
                                  <tbody>';
                                  if(!$return){
                                  //$output = json_decode($output, true);
                                  foreach($output as $key => $value){
                                  if($value == 'Waiting for file list from server'){
                                  $start = $key;
                                  $bool = true;
                                  }
                                  if($key > $start && $bool){
                                  $value_1 = explode(' ', $value.trim());
                                  // var_dump($value_1);
                                  echo '<tr><td>';
                                          echo $value_1[0];
                                          echo '</td><td>';
                                          echo $value_1[3];
                                          echo '</td><td>';
                                          echo '<a id="popoverData" class="btn" data-toggle="popover" title="Score" data-content="Score is '.round($value_1[6],2).'">Details</a>';
                                          echo '</td>';

                                      ob_start();
                                      require("operation.php");
                                      ob_end_flush();
                                      echo '</tr>';

                                  }
                                  }
                                  //print_r($output);
                                  }else {
                                  echo "Not done or Will not be done soon";
                                  }
                                  echo '</tbody>
                              </table>';
                              echo '</div>';
                          echo '</div>';
                      }
                    ?>

                  </div>
                    <div class="bhoechie-tab-content" id="uploadContentPanel">
                        <div class="col-md-4"></div>
                        <div class="col-md-4">
                            <form method="post" action="home.php" enctype="multipart/form-data">
                                <input type="file" id="files" name="files" accept=".txt"/>
                                <p style="text-align: right; margin-top: 20px;">
                                    <input type="submit" value="Upload Files" class="btn-lg btn-primary" />
                                </p>
                            </form>
                        </div>
                        <div class="col-md-4"></div>
                        <br>
                        <?php
                        if ($_FILES["files"]){
                        //                      die("In upload");
                            $target_dir = "/var/www/html/hoang/SemanticSearchClient/uploads/";
                            $file_dir = $target_dir . basename($_FILES["files"]["name"]);
                            $file_name = $_FILES["files"]["name"];
                            $file_size = $_FILES["files"]["size"];
                            $file_temp = $_FILES["files"]["tmp_name"];
                            // $file_t
                            if (move_uploaded_file($_FILES["files"]["tmp_name"], $file_dir)) {
                                $query_1 = "java -jar SemanticSearchClient.jar -u uploads";
                                exec($query_1, $output_1, $return);
                                if(!$return){
                                    $query_2 = "mv /var/www/html/hoang/SemanticSearchClient/uploads/*.* /var/www/html/hoang/cloud/cloudserver/storage/ ";
                                    exec($query_2, $output_2, $return);
                                    // print_r($output_1);
                                    if(!$return){
                                    $res = implode(" ", $output_1);

                                ?>
                                <script>
                                    $("div.bhoechie-tab-menu>div.list-group>a").siblings('a#searchPanel').removeClass("active");
                                    $("div.bhoechie-tab-menu>div.list-group>a").siblings('a#uploadPanel').addClass("active");
                                    $("div.bhoechie-tab>div#searchContentPanel").removeClass("active");
                                    $("div.bhoechie-tab>div#uploadContentPanel").addClass("active");
                                </script>
                                <div class='col-md-4 col-md-offset-4' style='margin-top:100px;'>
                                    Successfully Uploaded <?php echo $file_name; ?>
                                    <a id="popoverData" class="btn btn-primary" data-toggle="popover" title="Details" data-content="<?php echo $res;?>">
                                        Details
                                    </a>
                                </div>
                                <?php
                                    }
                                }
                            }
                            else{
                                ?>
                                <script>
                                    $("div.bhoechie-tab-menu>div.list-group>a").siblings('a#searchPanel').removeClass("active");
                                    $("div.bhoechie-tab-menu>div.list-group>a").siblings('a#uploadPanel').addClass("active");
                                    $("div.bhoechie-tab>div#searchContentPanel").removeClass("active");
                                    $("div.bhoechie-tab>div#uploadContentPanel").addClass("active");
                                </script>
                                <div class='col-md-4 col-md-offset-4' style='margin-top:100px;'>
                                    Failed to uploaded <?php echo $file_name; ?>
                                    <a id="popoverData" class="btn btn-primary" data-toggle="popover" title="Details" data-content="<?php print_r($_FILES) ;?>">
                                        Details
                                    </a>
                                </div>
                                <?php
                            }

                        }
                        ?>
                    </div>
<?php
                  if ($_POST["fileToRemove"]) {
                      $fileToRemove = $_POST["fileToRemove"];
                      $query_send = "java -jar SemanticSearchClient.jar -r \"" . $fileToRemove . "\"";
                      exec($query_send, $output_1, $return);

                      if(!$return){
                        $res = implode(" ", $output_1);
                        echo "<center>";
                        echo "<div class='row'>Successfully Removed ".$fileToRemove."</div>";
                        echo '<a id="popoverData" class="btn btn-primary" data-toggle="popover" title="Details" data-content="'.$res.'">Details</a>';
                        echo "</center>";
                        // print_r($output);
                        // echo "</span>";
                      }

                    else{
                      echo "Fail";
                    }

                  }
                  elseif($_POST["startServer"]){
                    // die();
                    echo "Starting Server";
                    $query_send = "java -jar ../SemanticSearchCloud/SemanticSearchCloud.jar &";
                    $query_send_pre = 'ps aux | grep "java -jar ../SemanticSearchCloud/SemanticSearchCloud.jar"';
                    exec($query_send, $output, $return);
                    // if(!$return){
                    //   if(sizeof($output)>2){
                        ?>
                        <script>
                          document.getElementById("start_pause_icon").className = "glyphicon glyphicon-pause";
                          document.getElementById("status").innerHTML = "Pause";
                        </script>
                        <?php
                  }
                  ?>

              </div>
            </div>
          </div>



          <?php
            include("footer.php");
          ?>
