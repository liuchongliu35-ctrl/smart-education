import ReactMarkdown from 'react-markdown';

export function MarkdownToHtml({ content,nowRef }) {
  return (
    <div className="markdown-content" ref={nowRef}>
      <ReactMarkdown>{content}</ReactMarkdown>
    </div>
  );
}