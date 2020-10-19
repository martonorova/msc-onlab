import React, { useState, useContext } from 'react';
import { Form, InputNumber, Button, Input, Select } from 'antd';

import JobContext from '../../context/job/jobContext';

const Home = () => {
  const [form] = Form.useForm();

  const jobContext = useContext(JobContext);

  const { postJob } = jobContext;

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onFinish = (values) => {
    console.log('Success:', values);
    postJob({ input: values.jobinput });
  };

  const onReset = () => {
    form.resetFields();
  };

  return (
    <Form
      form={form}
      name='control-hooks'
      onFinish={onFinish}
      onFinishFailed={onFinishFailed}
    >
      <Form.Item name='jobinput' label='Job input' rules={[{ required: true }]}>
        <InputNumber />
      </Form.Item>
      <Form.Item>
        <Button type='primary' htmlType='submit'>
          Submit
        </Button>
        <Button htmlType='button' onClick={onReset}>
          Reset
        </Button>
      </Form.Item>
    </Form>
  );
};

export default Home;
