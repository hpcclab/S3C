<?php
include("header.php");
session_start();

$_SESSION['new'] = true;
if(isset($_SESSION['ip']) && $_SESSION['ip'] == $_SERVER['HTTP_CLIENT_IP'])
    $_SESSION['new'] = false;
elseif (isset($_SERVER['HTTP_CLIENT_IP']) && $_SERVER['HTTP_CLIENT_IP']) {
	$_SESSION['ip'] = $_SERVER['HTTP_CLIENT_IP'];
}

$hitfile = 'hit.txt';
$count = fopen($hitfile,'r');
$count = fgets($count);

$count = (int) $count;
if ($_SESSION['new'])
    $count++;

$fw = fopen($hitfile,'w');
fwrite($fw,$count);
fclose($fw);

function search($query, $opt){
	if($opt == 4) // Added for by defaul:All options. previously Keyword opt value is 0 which is the reason to show keywords as default.
		$opt = 0;  // so thats why the value 4 is assigned but as client needs value 0 for keyword based search that's why this check is added.
	$query_send = "java -jar SemanticSearchEdge.jar -s \"" . $query . "\" " . $opt;
	$length = 2*strlen($query) + 40;
	exec($query_send, $output, $return);
	$start = null;
	$bool = false;
?>
<div class="container">
    <div class="row">
        <a id="popoverData" class="btn btn-primary" data-toggle="popover" title="Score" data-content="
        <?php
        	$count = 0;
        	foreach ($output as $key => $value){
            	if($value == 'Waiting for file list from server')
                	break;
            	else
                	$count++;
          	}
        	$var = $output[$count-1];
          	print_r($var);
          	$var = $output[$count-2];
          	print_r($var);
          	$var = $output[$count-3];
          	print_r($var);
         ?>"> Expanded query</a>
	</div>
</div>
<?php
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
        $non_zero = 0;
	
        foreach($output as $key => $value){
            if($value == 'Waiting for file list from server'){
                $start = $key;
                $bool = true;
            }
            if($key > $start && $bool){
                //$value_1 = explode(' ', $value.trim());
                $value_1 = explode(' ', trim($value));
                if(round($value_1[6],2) != 0){
                    $non_zero += 1;
                    echo '<tr><td>';
                    echo $value_1[0];
                    echo '</td><td>';
                    $fileName = explode('::', $value_1[3]);
                    echo $fileName[0];
                    echo '</td><td>';
                    echo '<a id="popoverData" class="btn" data-toggle="popover" title="Score" data-content="Score is '.round($value_1[6],2).'">Details</a>';
                    echo '</td>';
					ob_start();
                    require("operation.php");
                    ob_end_flush();
                    echo '</tr>';
                }
            }
        }
        if($non_zero == 0)
            echo "<h1>No relevant file! Please search for other query</h1>";
    } else {
        echo "Not done or Will not be done soon";
    }
    echo '</tbody></table>';
    echo '</div>';
    echo '</div>';
}
?>

<div class="container-custom">
    <div class="row">
        <div class="col-md-2 bhoechie-tab-menu">
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
    ?>
    <div class="row">
        <div class="bhoechie-tab">
            <div class="bhoechie-tab-content active" id="searchContentPanel">
                <div class="col-sm-6">
  	               <img class="img-responsive center-block" src="images/icon.jpg" style="max-height:220px;">
              	</div>
              	<div class="col-sm-6">
        		       <img class="img-responsive center-block" src="https://www.louisiana.edu/sites/louisiana/files/styles/large/public/news-images/2015-12/600by400logo.png?itok=r8YCOLPZ" style="max-height:220px;">
                </div>
                <center>
                	<div class="col align-self-center">
                		<h1>Secured Semantic Search over Encrypted Data in the Cloud </h1>
                	</div>
                </center>

                <form action="home.php" method="POST" id="searchForm">
                    <div class="search-box col-lg-6 col-lg-offset-3" id="SBox">
                        <select class="btn btn-default form-group" id="filter" name="opt">
                            <option value="1" <?php if(isset($_POST["opt"]) && $_POST["opt"] == 1){echo "selected";} ?>>All options</option> 
                            <option value="2" <?php if(isset($_POST["opt"]) && $_POST["opt"] == 2){echo "selected";} ?>>Synonym</option> 
                            <option value="3" <?php if(isset($_POST["opt"]) && $_POST["opt"] == 3){echo "selected";} ?>>Wikipedia</option>
                            <option value="4" <?php if(isset($_POST["opt"]) && $_POST["opt"] == 4){echo "selected";} ?>>Keyword Only</option>
                        </select>
                        <input id="search" name="query" placeholder="<?php if(isset($_POST["query"])){ echo $_POST["query"];}else{ echo "Search...";}?>" type="text">
                            <button class="also" onclick="searchSubmit();">
                                <span class="glyphicon glyphicon-search"></span>
                            </button>
                        <br>
                        <br>
                        <label>Numer of visitors: <?php echo $count;?></label>
                    </div>
                </form>
                <?php
                	if(isset($_POST["query"])){
                		$query = $_POST["query"];
                		$_SESSION['query'] = $query;
                		$opt = $_POST['opt'];
                		$_SESSION['opt'] = $opt;
                		search($query, $opt);
                	} else if(isset($_POST["back"])) {
                		search($_SESSION['query'], $_SESSION['opt']);
	      			}
                ?>

            </div>
            <div class="bhoechie-tab-content" id="uploadContentPanel">
                <div class="col-md-4"></div>
                <div class="col-md-4">
                    <form method="post" action="home.php" enctype="multipart/form-data" id="uploadform">
                        <input type="file" id="files" name="files" accept=".txt"/>
						<br>
						<br>
						<input type="radio" name="repository" value="-hc">HPCC<br>
						<input type="radio" name="repository" value="-gd">Google Drive<br>
       					<input type="radio" name="repository" value="-db">Dropbox<br>
			<center>
                        <p style="text-align: right; margin-top: 20px;">
				<!--<button type="button" onclick="showPopup()" class="btn-lg btn-primary" name="repository" value="-hc">HPCC</button>
				<button type="button" onclick="showPopup()" class="btn-lg btn-primary" name="repository" value="-gd">Google Drive</button>
				<button type="button" onclick="showPopup()" class="btn-lg btn-primary" name="repository" value="-db">Dropbox</button>-->
							<a  onclick="showPopup()" class="btn-lg btn-primary">Upload</a>
							<!--<a  onclick="showPopup()" class="btn-lg btn-primary" name="repository" value="-gd">Google Drive</a>
							<a  onclick="showPopup()" class="btn-lg btn-primary" name="repository" value="-db">Dropbox</a>-->
						</p>
			</center>
			    	</form>
                </div>
            </div>
        </div>
        <br>
     </div>
     <div class="col-md-4 col-md-offset-4">
	    <div id="popup" class="form-group">
	        <label>Enter Password:</label>
	        <input class="form-control" id="pass" type="password"/>
	        <button class="btn btn-primary" onclick="done()">Upload</button>
	        <button class="btn btn-danger" onclick="cancel()">Cancel</button>
	    </div>
	</div>
    <script type="text/javascript">
        function done() {
            document.getElementById("popup").style.display = "none";
            var password = document.getElementById("pass").value;
            console.log(password);
            if (password.localeCompare('hpccull16') == 0){
                document.getElementById("uploadform").submit();
                console.log("Yup");
            } else {
                alert("Invalid password");
                event.preventDefault();
            }
        }
        function showPopup() {
            document.getElementById("popup").style.display = "block";
        }
        function cancel(){
            document.getElementById("popup").style.display = "none";
        }
    </script>
	<?php
        if (isset($_FILES["files"])){
            $target_dir = "/home/cxf1714/public_html/S3C/S3Client/uploads/";
            $file_dir = $target_dir . basename($_FILES["files"]["name"]);
            $file_name = $_FILES["files"]["name"];
            $file_size = $_FILES["files"]["size"];
            $file_temp = $_FILES["files"]["tmp_name"];
		    $newLocation = "/home/cxf1714/public_html/S3C/cloud/cloudserver/storage/";
		    $repository = "-hc";

		    $key = "SemanticSearch";										// this is defined in both edit.php and here; will need to be consolidated later...
		    $cipherMethod = "DES-ECB";

            if (move_uploaded_file($file_temp, $file_dir)) {
				
				$query_2 = "java -jar SemanticSearchEdge.jar -k uploads";
				exec($query_2, $output_2, $return_2);
				if ($return_2)
					echo "Error making and hashing key files.";			
				if(isset($_POST["repository"])){
				    $repository = $_POST["repository"];
				}

			/*	if($repository == "-hc"){
				    $unencryptedFile = file_get_contents($file_dir);
				    unlink($file_dir);
				    $newFile = fopen($file_dir, "w");
				    $successfulEncrypt = openssl_encrypt($unencryptedFile, $cipherMethod, $key, OPENSSL_RAW_DATA);
				    fwrite($newFile, $successfulEncrypt);				
				    fclose($newFile);
				}*/

				$query_1 = "java -jar SemanticSearchEdge.jar -u uploads ".$repository;
                exec($query_1, $output_1, $return);
                if(!$return){
					$res = implode(" ", $output_1);
					?>
					<script>
						$("div.bhoechie-tab-menu>div.list-group>a").siblings('a#searchPanel').removeClass("active");
						$("div.bhoechie-tab-menu>div.list-group>a").siblings('a#uploadPanel').addClass("active");
						$("div.bhoechie-tab>div#searchContentPanel").removeClass("active");
						$("div.bhoechie-tab>div#uploadContentPanel").addClass("active");
					</script>
					<div class='col-md-4 col-md-offset-4' style='margin-top:100px;'>Successfully Uploaded <?php echo $file_name; ?>
						<a id="popoverData" class="btn btn-primary" data-toggle="popover" title="Details" data-content="<?php echo $res;?>">Details</a>
					</div>
					<?php
                } else {
					?>
					<script>
						$("div.bhoechie-tab-menu>div.list-group>a").siblings('a#searchPanel').removeClass("active");
						$("div.bhoechie-tab-menu>div.list-group>a").siblings('a#uploadPanel').addClass("active");
						$("div.bhoechie-tab>div#searchContentPanel").removeClass("active");
						$("div.bhoechie-tab>div#uploadContentPanel").addClass("active");
					</script>
					<div class='col-md-4 col-md-offset-4' style='margin-top:100px;'>Failed to uploaded <?php echo $file_name; ?>
						<a id="popoverData" class="btn btn-primary" data-toggle="popover" title="Details" data-content="<?php print_r($_FILES) ;?>">Details</a>
					</div>
					<?php
				}

                if ($repository == "-hc"){
                	
                } else {
                	unlink($newLocation.$file_name);
                }
				
			}
    ?>
</div>
<?php
		}
    if (isset($_POST["fileToRemove"])) {
        $fileToRemove = $_POST["fileToRemove"];
        $query_send = "java -jar SemanticSearchEdge.jar -r \"" . $fileToRemove . "\"";
        exec($query_send, $output_1, $return);

        if(!$return){
            $res = implode(" ", $output_1);
            echo "<center>";
            echo "<div style='visibility: hidden;'>Semantic Searafdasdfasfdasdf</div>";
            echo '<a id="popoverData" class="btn btn-primary" data-toggle="popover" title="Details" data-content="'.$res.'">Details</a>';
            echo "</center>";
        } else {
            echo "Fail";
        }

    } elseif (isset($_POST["startServer"])) {
        echo "Starting Server";
        $query_send = "java -jar ../SemanticSearchCloud/SemanticSearchCloud.jar &";
        $query_send_pre = 'ps aux | grep "java -jar ../SemanticSearchCloud/SemanticSearchCloud.jar"';
        exec($query_send, $output, $return);
    }
    include("footer.php");
?>
