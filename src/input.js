let a = 5;
let result = a + 10;

if (result > 10) {
    console.log("Результат больше 10");
} else {
    console.log("Результат меньше или равен 10");
}

for (let i = 0; i < 5; i++) {
    console.log("word");
}

function okay(line) {
    for (let k = 0; k < 5; k++) {
        if(k % 2 != 0){
            console.log("-----------")}
        console.log(line)
    }
    return line
};

okay("it is okay")