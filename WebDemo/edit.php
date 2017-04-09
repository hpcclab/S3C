<?php
include("header.php");
// configuration
$url = 'edit.php';
$file = './data/output_decrypted/';

// check if form has been submitted
if ($_POST['text'])
{
  // die("In here");
  $file = $_POST['file'];
  $thefile = str_replace("./data/output_decrypted/","",$file);
  // die($file);
    // save the text contents
    file_put_contents($file, $_POST['text']);
    // die($file);
    $query_move = "mv " . $file. " uploads/".$thefile;
    exec($query_move, $output_move, $return_move);

    if(!$return_move){
      $query = "java -jar SemanticSearchClient.jar -r \"".$thefile."\"";
      exec($query, $output1, $return);
      if(!$return){
        $query_1 = "java -jar SemanticSearchClient.jar -u uploads";
        exec($query_1, $output_1, $return);
        if(!$return){
          // print_r($output_1);
        }
      }
    }
    // redirect to form again
    header(sprintf('Location: %s', $url."?file-to-edit=".$thefile));
    printf('<a href="%s">Moved</a>.', htmlspecialchars($url));
    exit();
}
if($_GET['file-to-edit']){
  $filename = $_GET['file-to-edit'];
  // echo $filename;
  // die($filename);
  $file = $file . $filename;
  // echo $file;
  $query = "cp ../cloud/cloudserver/storage/" . $filename . " ./data/input_encrypted";

  // echo $query;
  exec($query, $output, $return);

  // print_r($output);
  if(!$return){
    // echo "Query moving done";
    $query_1 = "java -jar SemanticSearchClient.jar -d \"" . $filename . "\"";

    // echo $query_1;
    exec($query_1, $output, $return);

    if(!$return){
      // echo "Done!";
    }
  }



  // echo $file;
}

// read the textfile
$text = file_get_contents($file, true);

?>
<!-- HTML form -->
<div class="container">
  <div class="row">
<!--    <div class="col"></div>-->
    <div class="col-lg-6 col-lg-offset-2">
      <form action="edit.php" method="post">
        <textarea name="text" rows="30" style="margin-top:50px; width:800px; height:800px" class="form-control"><?php echo htmlspecialchars($text) ?></textarea>
        <br>
        <input type="hidden" name="file" value="<?php echo $file;?>"/>
        <button type="submit" class="btn btn-primary">Save Changes</button>
        <a onclick="history.go(-1);return true;" class="btn btn-info">Back</a>
      </form>
    </div>
<!--    <div class="col"></div>-->
  </div>
</div>
<?php
include("footer.php");
?>
