import { h, render } from 'https://esm.sh/preact';
import { useState, useEffect } from 'https://esm.sh/preact/hooks';

// Helper function to determine if a comment tree has any non-author responses
const hasNonAuthorResponses = (comment, originalAuthor) => {
  if (!comment) return false;
  
  // Check if the current comment is from someone other than the original author
  if (comment.post.author.handle !== originalAuthor) {
    return true;
  }
  
  // Recursively check replies
  if (comment.replies && comment.replies.length > 0) {
    return comment.replies.some(reply => hasNonAuthorResponses(reply, originalAuthor));
  }
  
  return false;
};

// Filter function to clean up the thread
const filterThreadComments = (comments, originalAuthor) => {
  if (!comments) return [];
  
  return comments
    // First, filter out any top-level comments that are:
    // 1. From the original author AND
    // 2. Don't have any non-author responses in their reply tree
    .filter(comment => 
      comment.post.author.handle !== originalAuthor || 
      hasNonAuthorResponses(comment, originalAuthor)
    )
    // Then recursively process any replies
    .map(comment => ({
      ...comment,
      replies: comment.replies ? filterThreadComments(comment.replies, originalAuthor) : []
    }));
};

const Comment = ({ comment }) => {
  const postLink = `https://bsky.app/profile/${comment.post.author.handle}/post/${comment.post.uri?.split('/').pop()}`;
  
  return h('a', { 
    href: postLink,
    target: '_blank',
    rel: 'noopener noreferrer',
    class: 'block no-underline text-inherit font-normal'
  }, [
    h('div', { class: 'flex gap-3 border rounded-lg hover:bg-gray-50 border-slate-100 p-4' }, [
      h('div', { class: 'flex-shrink-0' }, [
        h('img', {
          src: comment.post.author.avatar,
          alt: `${comment.post.author.displayName}'s avatar`,
          class: 'w-10 h-10 rounded-full object-cover'
        })
      ]),
      h('div', { class: 'flex-1 min-w-0' }, [
        h('div', { class: 'flex items-center gap-1' }, [
          h('span', { class: 'font-medium text-gray-900 truncate' }, comment.post.author.displayName),
          h('span', { class: 'text-gray-500 truncate' }, `@${comment.post.author.handle}`)
        ]),
        h('div', { class: 'mt-1 text-gray-900' }, comment.post.record.text),
        h('div', { class: 'mt-2 flex items-center gap-4 text-sm text-gray-500' }, [
          h('span', null, `${comment.post.replyCount || 0} 💬`),
          h('span', null, `${comment.post.repostCount || 0} 🔁`),
          h('span', null, `${comment.post.likeCount || 0} ❤️`),
          h('time', { 
            datetime: comment.post.indexedAt,
            class: 'ml-auto text-gray-500' 
          }, new Date(comment.post.indexedAt).toLocaleDateString())
        ])
      ])
    ]),
    comment.replies?.length > 0 && 
      h('div', { class: 'ml-12 mt-2' }, 
        comment.replies.map(reply => h(Comment, { comment: reply }))
      )
  ]);
};

const BlueskyComments = ({ uri }) => {
    const [comments, setComments] = useState([]);
  const [error, setError] = useState(null);
  const [originalAuthor, setOriginalAuthor] = useState(null);

  useEffect(() => {
    const fetchComments = async () => {
      if (!uri) return;
      try {
        const endpoint = `https://api.bsky.app/xrpc/app.bsky.feed.getPostThread?uri=${encodeURIComponent(uri)}`;
        const response = await fetch(endpoint, {
          method: 'GET',
          headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) throw new Error(`Failed to fetch comments: ${response.status}`);
        const data = await response.json();
        
        if (data.thread?.post?.author?.handle) {
          setOriginalAuthor(data.thread.post.author.handle);
        }
        
        if (data.thread?.replies) {
          // Filter the thread before setting comments
          const filteredComments = filterThreadComments(data.thread.replies, data.thread.post.author.handle);
          setComments(filteredComments);
        }
      } catch (e) {
        setError(e.message);
        console.error('Error:', e);
      }
    };
    fetchComments();
  }, [uri]);

  const postId = uri.split('/').pop();
  const blueskyLink = `https://bsky.app/profile/${uri.split('/')[2]}/post/${postId}`;

  return h('div', null, [
    h('p', {className: "prose text-center mb-6 min-w-full"}, [
      h('a', { href: blueskyLink, target: '_blank', rel: 'noopener noreferrer' }, 'Reply on Bluesky'),
      ' to join the conversation.'
    ]),
    error && h('p', null, 'Error loading comments: ' + error),
    // !error && comments.length === 0 && h('p', {className: "text-center"}, 'No comments yet. Be the first to comment!'),
    !error && comments.length > 0 && h('div', null,
      comments.map(comment => h(Comment, { comment }))
    )
  ]);
};

function init() {
  const scripts = document.querySelectorAll('script[data-bluesky-uri]');
  scripts.forEach(script => {
    const uri = script.getAttribute('data-bluesky-uri');
    if (!uri) {
      console.error('Bluesky Comments: Missing data-bluesky-uri attribute');
      return;
    }
    const container = document.createElement('div');
    script.parentNode.insertBefore(container, script.nextSibling);
    render(h(BlueskyComments, { uri }), container);
  });
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
