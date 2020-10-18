const express = require('express');
const router = express.Router();

const fib = require('../algorithms/fibonacci');

router.post('/', (req, res) => {
  const { input } = req.body;

  const result = fib(input);

  res.json({
    ...req.body,
    result,
  });
});

module.exports = router;
