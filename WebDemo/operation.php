<td>
  <?php
  $realName = explode("::",$value_1[3]);
  ?>
  <a href="edit.php?file-to-edit=<?php echo $value_1[3];?>" title="Edit">
    <span class="glyphicon glyphicon-pencil"></span>
  </a>
  <a id="<?php echo $value_1[3];?>" class = "removeLink__" title="Remove this file">
    <span class="glyphicon glyphicon-remove" id="<?php echo $value_1[3];?>">
      <form action="home.php" method="post" id="removeForm<?php echo $value_1[3];?>" onsubmit="return somefunction('removeForm<?php echo $value_1[3];?>');">
        <input type="hidden" name="fileToRemove" value="<?php echo $value_1[3];?>"/>
      </form>
    </span>
  </a>
  <?php
     if($realName[1] == "-hc"){
  ?>
      <a href="../cloud/cloudserver/storage/<?php echo $realName[0];?>" title="Download encrypted file">
        <span class="glyphicon glyphicon-cloud-download"></span>
      </a>
  <?php
      } else {
	$whichqueryisthis = "java -jar SemanticSearchEdge.jar -f ".$realName[1]." ".$realName[0];
	exec($whichQueryIsThis);
  ?>
	<a href="./data/inputEncrypted/<?php echo $realName[0];?>" title="Download encrypted file">
	  <span class="glyphicon glyphicon-cloud-download"></span>
	</a>
  <?php } ?>
  <a href="../cloud/cloudserver/storage/<?php echo str_replace('.txt','.key',$realName[0]);?>" title="Download key file">
    <span class="glyphicon glyphicon-compressed"></span>
  </a>
</td>
