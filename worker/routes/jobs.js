const express = require('express');
const router = express.Router();

const fib = require('../algorithms/fibonacci');

router.post('/', (req, res) => {
  const { input, id } = req.body;

  console.log(`Start calculating result for job ${id}`);

  console.time('Fibonacci');
  const result = fib(input);
  console.timeEnd('Fibonacci');
   
  console.log(`Calculated result for job ${id}: ${result}`);

  res.json({
    ...req.body,
    result,
  });
});

module.exports = router;
