const express = require("express");
const app = express();
var bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended : true}));

mysql = require('mysql');
var connection = mysql.createConnection({
	host: 'localhost',
	user: 'kcalmera',
	password: '5252',
	database: 'kcalmera_db'
});
connection.connect();

app.post("/data", function (req, res) {
	console.log("Start to insert user diet data...");
	console.log(req.body);
	connection.query(`INSERT INTO diet_diary 
		(sex, age, height, weight, time, food_name, gram, kcal,carbohydrate,
		protein, fat, sugars, natrium, cholesterol, fatty_acid, trans_fat)
		VALUES (${req.body.sex}, ${req.body.age}, ${req.body.height}, ${req.body.weight},
			${req.body.time}, ${req.body.food_name}, ${req.body.gram}, ${req.body.kcal},
			${req.body.carbohydrate}, ${req.body.protein}, ${req.body.fat},
			${req.body.sugars}, ${req.body.natrium}, ${req.body.cholesterol},
			${req.body.fatty_acid}, ${req.body.trans_fat})`);	
	res.send("done.");
});

app.listen(80, function() { 
	console.log("Listening Port 80...");
});
