const express = require('express');
const router = express.Router();

const fib = require('../algorithms/fibonacci');

router.post('/', (req, res) => {
  const { input, id } = req.body;

  console.log(`Start calculating result for job ${id}`);

  const result = fib(input);

  console.log(`Calculated result for job ${id}: ${result}`);

  res.json({
    ...req.body,
    result,
  });
});

module.exports = router;
