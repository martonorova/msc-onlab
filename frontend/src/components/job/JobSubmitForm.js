import React, { useContext } from 'react';
import { Form, InputNumber, Button, Card, Space, Modal } from 'antd';

import JobContext from '../../context/job/jobContext';

const JobSubmitForm = () => {
  const [form] = Form.useForm();

  const jobContext = useContext(JobContext);

  const { postJob, deleteJobs } = jobContext;

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onFinish = (values) => {
    console.log('Success:', values);
    postJob({ input: values.jobinput });
    onReset();
  };

  const onReset = () => {
    form.resetFields();
  };

  const { confirm } = Modal;
  const clearAllJobs = () => {

    confirm({
      title: 'Are you sure you want to delete ALL jobs?',
      content: 'This cannot be undone',
      onOk() {
        deleteJobs();
      }
    })
  }

  return (
    <Card title='Submit new job' style={{ width: '500px' }}>
      <Form
        form={form}
        name='control-hooks'
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
      >
        <Form.Item
          name='jobinput'
          label='Job input'
          rules={[{ required: true }]}
        >
          <InputNumber size='large' autoFocus={true} style={{ width: '80%' }} />
        </Form.Item>
        <Space size='middle' align='end'>
          <Form.Item>
            <Button type='primary' htmlType='submit'>
              Submit
            </Button>
          </Form.Item>
          <Form.Item>
            <Button htmlType='button' onClick={onReset}>
              Reset
            </Button>
          </Form.Item>
        </Space>
      </Form>
      <Button type='default' onClick={clearAllJobs}>Clear All Jobs</Button>
    </Card>
  );
};

export default JobSubmitForm;
