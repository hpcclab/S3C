<?php
  include("header.php");

  // configuration
  $url = 'edit.php';
  $file = './data/inputEncrypted/';
  $key = 'SemanticSearch';
  $cipherMethod = 'DES-ECB';

  // check if form has been submitted
  if (isset($_POST['text'])) {
    $file = $_POST['file'];
    $realName = explode('::', $file);                   // Separate repositiory from file name.

    $thefile = str_replace("./data/inputEncrypted/","",$realName[0]);

    // save the text contents
    file_put_contents($realName[0], $_POST['text']);

    $query_move = "mv " . $realName[0]. " uploads/ ".$thefile;
    exec($query_move, $output_move, $return_move);
    if(!$return_move){
      $query = "java -jar SemanticSearchEdge.jar -r \"".$thefile."::".$realName[1]."\"";
      exec($query, $output1, $return);
      if(!$return){
        $query_1 = "java -jar SemanticSearchEdge.jar -u uploads " .$realName[1];			// hmmmmmmm this may cause an issue
        exec($query_1, $output_1, $return);
      }
    }

    // redirect to form again
    header(sprintf('Location: %s', $url."?file-to-edit=".$thefile));
    printf('<a href="%s">Moved</a>.', htmlspecialchars($url));
    exit();
  }
  if($_GET['file-to-edit']){
    $filename = $_GET['file-to-edit'];
    $realName = explode('::', $filename);                   // Separate repositiory from file name.
    $file = $file.$realName[0];
    if ($realName[1] == "-hc"){
    	$queryFileToEdit = "cp ../cloud/cloudserver/storage/" .$realName[0]. " ./data/inputEncrypted/";

    	exec($queryFileToEdit, $output, $return);

	if($return){
	    echo "Failure to copy to inputEncrypted.";
	}
    } else {
	$fetchQuery = "java -jar SemanticSearchEdge.jar -f ".$realName[1]." ".$realName[0];
	exec($fetchQuery, $output2, $return2);
	if($return2){
	    echo "Failure to move outside repository file to inputEncrypted";
	}
    }

    $encryptedFile = file_get_contents($file);

    $decryptedFile = openssl_decrypt($encryptedFile, $cipherMethod, $key, OPENSSL_RAW_DATA|OPENSSL_ZERO_PADDING);

    if($decryptedFile == FALSE){
      echo "Error decrypting document!";
      $text = null;
    } else {
      $text = $decryptedFile;
    }
  }
?>
<!-- HTML form -->
<div class="container">
  <div class="row">
    <div class="col-lg-6 col-lg-offset-2">
      <form action="edit.php" method="post">
        <textarea name="text" rows="30" style="margin-top:50px; width:800px; height:800px" class="form-control"><?php echo htmlspecialchars($text) ?></textarea>
        <br>
        <input type="hidden" name="file" value="<?php echo $filename;?>"/>
        <button type="submit" class="btn btn-primary">Save Changes</button>
      </form>
      <br>
      <!--<form action="https://teaching.cmix.louisiana.edu/~cxf1714/S3C/S3Client/home.php" method="post">-->
      <form action="home.php" method="post">
      <button name = "back" type = "submit" class = "btn btn-info" value = "back">Back</button>
      </form>
    </div>
  </div>
</div>

<?php
include("footer.php");
?>
