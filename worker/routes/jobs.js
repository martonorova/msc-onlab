const express = require('express');
const router = express.Router();

const fib = require('../algorithms/fibonacci');

// let isBusy = false;

router.post('/', (req, res) => {
  const { input, id } = req.body;
  if (typeof(input) === 'undefined' || typeof(id) === 'undefined') {
    // return 204 (instead of 400), because K8s 
    res.status(400).json({ 'message': 'Insufficient data'});
  }

  console.log(`Start calculating result for job ${id}`);

  console.time('Fibonacci');
  // isBusy = true;
  const result = fib(input);
  // isBusy = false;
  console.timeEnd('Fibonacci');
   
  console.log(`Calculated result for job ${id}: ${result}`);

  res.json({
    ...req.body,
    result,
  });
});

router.get('/busy', (req, res) => {
  console.log('HEALTH CHECK');
  res.status(200).json({ 'message': 'Worker now ready'});
  // if (false) {
  //   res.status(503).json({ 'message': 'Worker busy'});
  // } else {
  //   res.status(200).json({ 'message': 'Worker now ready'});
  // }
})

module.exports = router;
