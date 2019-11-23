<?php

$subject = $_POST['subject'];
$message = $_POST['message'];

$headers = 'From: <mfebinthomas@gmail.com>' . "\r\n" .
        'Reply-To: <mfebinthomas@gmail.com>' . "\r\n" .
        'X-Mailer: PHP/' . phpversion();

$receiver_email = "fmt.febulous@gmail.com";

if(mail($receiver_email, $subject, $message, $headers))
	echo "Thank You for your Message, kindly wait until our Team responds to it !";

else
	echo "Sending Email Failed !";
 
?>