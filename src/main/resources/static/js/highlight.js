document.addEventListener('DOMContentLoaded', function () {
    var expirationCells = document.querySelectorAll('.expiration-cell');

    expirationCells.forEach(function (cell, index) {
        var expirationDate = new Date(cell.textContent);
        var currentDate = new Date();
        var sevenDaysFromNow = new Date();
        sevenDaysFromNow.setDate(currentDate.getDate() + 7);

        // First apply the red or yellow color based on expiration date
        if (expirationDate < currentDate) {
            cell.style.backgroundColor = 'red';
        } else if (expirationDate < sevenDaysFromNow) {
            cell.style.backgroundColor = 'yellow';
        } else {
            if (index % 2 === 1) {
                cell.style.backgroundColor = '#dddddd85';
            } else {
                cell.style.backgroundColor = 'white';
            }
        }
    });
});
